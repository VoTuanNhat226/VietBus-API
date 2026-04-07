package com.vtn.service;

import com.vtn.dto.request.TripRequest;
import com.vtn.entity.*;
import com.vtn.enumdef.TripSeatStatusEnum;
import com.vtn.enumdef.TripStatusEnum;
import com.vtn.repository.*;
import com.vtn.utils.BaseResponse;
import com.vtn.utils.CodeGeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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

    public BaseResponse getAllTrips(TripRequest request) {
        List<TripEntity> trips = tripRepository.getAllByCondition(
                    request.getFromStationId(),
                    request.getToStationId(),
                    request.getDriverId(),
                    request.getVehicleId(),
                    request.getStatus(),
                    request.getTripCode()
            );
        return new BaseResponse(200, trips, "Get all trips successful",null,null);
    }

    public BaseResponse getAllTripOpenBooking() {
        log.info("Get trips open for booking");
        List<TripEntity> trips = tripRepository.getAllTripOpenBooking(TripStatusEnum.OPEN_FOR_BOOKING.toString());
        return new BaseResponse(200, trips, "Get all trips open for booking successful",null,null);
    }

    public BaseResponse getTripByTripId(TripRequest request) {
        log.info("Get trip by tripId: {}", request.getTripId());
        if (request.getTripId() == null) {
            log.warn("TripId is required");
            return new BaseResponse(400, null, "TripId is required", null, null);
        }
        TripEntity trip = tripRepository.findById(request.getTripId()).orElse(null);
        if(trip == null) {
            log.error("Trip not found with id: {}", request.getTripId());
            return new BaseResponse(404, null,"Trip not found",null,null);
        }
        return new BaseResponse(200, trip, "Get trip successfully",null,null);
    }

    @Transactional
    public BaseResponse createTrip(TripRequest tripRequest) {
        log.info("Start createTrip with request: {}", tripRequest);
        UserDetails info = getInfo();
        log.info("User {} is creating trip", info.getUsername());
        try {
            RouteEntity route = routeRepository.findByRouteId(tripRequest.getRouteId());
            if (route == null) {
                return new BaseResponse(400,null,"Route not found",null,null);
            }

            VehicleEntity vehicle = vehicleRepository.findByVehicleId(tripRequest.getVehicleId());
            if (vehicle == null) {
                return new BaseResponse(400,null,"Vehicle not found",null,null);
            }

            EmployeeEntity driver = employeeRepository.findByEmployeeId(tripRequest.getDriverId());
            if (driver == null) {
                return new BaseResponse(400,null,"Driver not fount",null,null);
            }

            if (tripRequest.getArrivalTime().isBefore(tripRequest.getDepartureTime())) {
                return new BaseResponse(400, null, "Arrival time and Departure time are invalid", null, null);
            }

            if (tripRequest.getDepartureTime().isBefore(LocalDateTime.now())) {
                return new BaseResponse(400,null,"Departure time must be longer than current time",null,null);
            }

            if (tripRequest.getPrice() == null || tripRequest.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return new BaseResponse(400,null,"Ticket prices invalid",null,null);
            }

            boolean conflict = tripRepository.existsDriverConflict(
                    driver.getEmployeeId(),
                    route.getRouteId(),
                    tripRequest.getDepartureTime(),
                    tripRequest.getArrivalTime()
            );
            if (conflict) {
                return new BaseResponse(400,null,"The driver has been assigned to another trip on the same route during this time",null,null);
            }

            boolean vehicleConflict = tripRepository.existsVehicleConflict(
                    vehicle.getVehicleId(),
                    tripRequest.getDepartureTime(),
                    tripRequest.getArrivalTime()
            );

            if (vehicleConflict) {
                return new BaseResponse(400,null,"The vehicle has been assigned to another trip during this time",null,null);
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
            trip.setStatus(TripStatusEnum.CREATED);
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
                        ts.setStatus(TripSeatStatusEnum.AVAILABLE);
                        return ts;
                    }).toList();
            tripSeatRepository.saveAll(tripSeats);

            return new BaseResponse(200,trip,"Create trip successful",null,null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BaseResponse updateTrip(TripRequest request) {
        log.info("Start updateTrip with request: {}", request);
        UserDetails info = getInfo();
        log.info("User {} is updating trip", info.getUsername());

        TripEntity trip = tripRepository.findById(request.getTripId()).orElse(null);
        if (trip == null) {
            log.error("Trip not found with id: {}", request.getTripId());
            return new BaseResponse(404,null,"Trip not found", null, null);
        }

        trip.setStatus(request.getStatus());
        trip.setUpdatedBy(info.getUsername());
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);
        log.info("Trip updated successful with id: {}", trip.getTripId());

        return new BaseResponse(200,trip,"Update trip successful", null, null);
    }

    private boolean isAdmin(UserDetails info) {
        return info.getAuthorities()
                .stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private UserDetails getInfo() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
