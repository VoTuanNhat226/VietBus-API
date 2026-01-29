package com.vtn.service;

import com.vtn.dto.request.EmployeeRequest;
import com.vtn.dto.request.TripRequest;
import com.vtn.entity.*;
import com.vtn.repository.*;
import com.vtn.utils.BaseResponse;
import com.vtn.utils.CodeGeneratorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TripService {
    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final EmployeeRepository employeeRepository;
    private final VehicleRepository vehicleRepository;
    private final SeatRepository seatRepository;
    private final TripSeatRepository tripSeatRepository;

    @Autowired
    public TripService(TripRepository tripRepository,
                       RouteRepository routeRepository,
                       EmployeeRepository employeeRepository,
                       VehicleRepository vehicleRepository,
                       SeatRepository seatRepository,
                       TripSeatRepository tripSeatRepository) {
        this.tripRepository = tripRepository;
        this.routeRepository = routeRepository;
        this.employeeRepository = employeeRepository;
        this.vehicleRepository = vehicleRepository;
        this.seatRepository = seatRepository;
        this.tripSeatRepository = tripSeatRepository;
    }

    private boolean isAllParametersNull(TripRequest request) {
        return ((request.getFromStationId() == null) &&
                (request.getToStationId() == null) &&
                (request.getDriverId() == null) &&
                (request.getVehicleId() == null) &&
                (request.getStatus() == null) &&
                (request.getTripCode() == null));
    }

    public BaseResponse getAllTrips(TripRequest request) {
        List<TripEntity> trips;
        if (isAllParametersNull(request)) {
            trips = tripRepository.findAll();
        } else {
            trips = tripRepository.getAllByCondition(
                    request.getFromStationId(),
                    request.getToStationId(),
                    request.getDriverId(),
                    request.getVehicleId(),
                    request.getStatus(),
                    request.getTripCode()
            );
        }
        return new BaseResponse(200, trips, "Get all trips successfully","Get all trips successfully",null);
    }

    public BaseResponse getAllTripOpenBooking() {
        try {
            List<TripEntity> trips = tripRepository.getAllTripOpenBooking("OPEN_FOR_BOOKING");
            return new BaseResponse(200, trips, "Get all trip can sell successfully",null,null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BaseResponse getTripByTripId(TripRequest request) {
        try {
            TripEntity trip = tripRepository.findById(request.getTripId()).orElse(null);
            if(trip == null) {
                return new BaseResponse(400, null,"Không tìm thấy chuyến xe",null,null);
            }
            return new BaseResponse(200, trip, "Get trip successfully",null,null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public BaseResponse createTrip(TripRequest tripRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            RouteEntity route = routeRepository.findByRouteId(tripRequest.getRouteId());
            if (route == null) {
                return new BaseResponse(400,null,"Không tìm thấy tuyến xe",null,null);
            }

            VehicleEntity vehicle = vehicleRepository.findByVehicleId(tripRequest.getVehicleId());
            if (vehicle == null) {
                return new BaseResponse(400,null,"Không tìm thấy xe",null,null);
            }

            EmployeeEntity driver = employeeRepository.findByEmployeeId(tripRequest.getDriverId());
            if (driver == null) {
                return new BaseResponse(400,null,"Không tìm thấy tài xế",null,null);
            }

            if (tripRequest.getArrivalTime().isBefore(tripRequest.getDepartureTime())) {
                return new BaseResponse(400, null, "Thời gian không hợp lệ", null, null);
            }

            if (tripRequest.getDepartureTime().isBefore(LocalDateTime.now())) {
                return new BaseResponse(400,null,"Thời gian xuất bến phải lớn hơn hiện tại",null,null);
            }

            if (tripRequest.getPrice() == null || tripRequest.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return new BaseResponse(400,null,"Giá vé không hợp lệ",null,null);
            }

            boolean conflict = tripRepository.existsDriverConflict(
                    driver.getEmployeeId(),
                    route.getRouteId(),
                    tripRequest.getDepartureTime(),
                    tripRequest.getArrivalTime()
            );
            if (conflict) {
                return new BaseResponse(400,null,"Tài xế đã được phân công cho chuyến khác cùng tuyến trong khoảng thời gian này",null,null);
            }

            boolean vehicleConflict = tripRepository.existsVehicleConflict(
                    vehicle.getVehicleId(),
                    tripRequest.getDepartureTime(),
                    tripRequest.getArrivalTime()
            );

            if (vehicleConflict) {
                return new BaseResponse(400,null,"Xe đã được phân công cho chuyến khác trong khoảng thời gian này",null,null);
            }

            TripEntity trip = new TripEntity();
            String tripCode;
            do {
                tripCode = CodeGeneratorUtil.generateCode();
            } while (tripRepository.existsByTripCode(tripCode));

            trip.setTripCode(tripCode);

            trip.setDepartureTime(tripRequest.getDepartureTime());
            trip.setArrivalTime(tripRequest.getArrivalTime());
            trip.setRestTime(tripRequest.getRestTime());
            trip.setPrice(tripRequest.getPrice());
            trip.setStatus("CREATED");
            trip.setRestStop(tripRequest.getRestStop());
            trip.setRoute(route);
            trip.setVehicle(vehicle);
            trip.setDriver(driver);
            trip.setCreatedBy(info.getUsername());
            trip.setCreatedAt(LocalDateTime.now());
            tripRepository.save(trip);

            List<SeatEntity> seats = seatRepository.findByVehicleId(vehicle.getVehicleId());

            List<TripSeatEntity> tripSeats = seats.stream()
                    .map(seat -> {
                        TripSeatEntity ts = new TripSeatEntity();
                        ts.setTrip(trip);
                        ts.setSeat(seat);
                        ts.setStatus("AVAILABLE");
                        return ts;
                    }).toList();
            tripSeatRepository.saveAll(tripSeats);

            return new BaseResponse(200,trip,"Create trip successfully","Create trip successfully",null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
