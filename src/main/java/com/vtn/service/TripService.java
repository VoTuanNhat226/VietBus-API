package com.vtn.service;

import com.vtn.dto.request.TripRequest;
import com.vtn.dto.response.TripResponse;
import com.vtn.entity.*;
import com.vtn.entity.log.TripLog;
import com.vtn.enumdef.AccountRoleEnum;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TripService {
    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final EmployeeRepository employeeRepository;
    private final VehicleRepository vehicleRepository;
    private final SeatRepository seatRepository;
    private final TripSeatRepository tripSeatRepository;
    private final TripLogRepository tripLogRepository;

    @Autowired
    public TripService(TripRepository tripRepository,
                       RouteRepository routeRepository,
                       EmployeeRepository employeeRepository,
                       VehicleRepository vehicleRepository,
                       SeatRepository seatRepository,
                       TripSeatRepository tripSeatRepository,
                       TripLogRepository tripLogRepository) {
        this.tripRepository = tripRepository;
        this.routeRepository = routeRepository;
        this.employeeRepository = employeeRepository;
        this.vehicleRepository = vehicleRepository;
        this.seatRepository = seatRepository;
        this.tripSeatRepository = tripSeatRepository;
        this.tripLogRepository = tripLogRepository;
    }

    public BaseResponse getAllTrips(TripRequest request) {
        List<TripEntity> trips = tripRepository.getAllByCondition(
                request.getFromStationId(),
                request.getToStationId(),
                request.getVehicleId(),
                request.getStatus(),
                request.getTripCode(),
                request.getDriverId()
        );
        List<TripResponse> result = trips.stream()
                .map(this::toResponse)
                .toList();
        return new BaseResponse(200, result, "Get all trips successful", null, null);
    }

    public BaseResponse getAllTripOpenBooking() {
        List<TripEntity> trips = tripRepository.getAllTripByStatus(TripStatusEnum.OPEN_FOR_BOOKING);
        List<TripResponse> result = trips.stream()
                .map(this::toResponse)
                .toList();
        return new BaseResponse(200, result, "Get all trips open for booking successful",null,null);
    }

    public BaseResponse getTripByTripId(TripRequest request) {
        if (request.getTripId() == null) {
            return new BaseResponse(400, null, "TripId is required", null, null);
        }
        TripEntity trip = tripRepository.findById(request.getTripId()).orElse(null);
        if(trip == null) {
            return new BaseResponse(404, null,"Trip not found",null,null);
        }
        TripResponse response = toResponse(trip);
        return new BaseResponse(200, response, "Get trip successfully",null,null);
    }

    @Transactional
    public BaseResponse createTrip(TripRequest tripRequest) {
        UserDetails info = getInfo();

        // Validate route
        RouteEntity route = routeRepository.findByRouteId(tripRequest.getRouteId());
        if (route == null) {
            return new BaseResponse(404,null,"Route not found",null,null);
        }
        // Validate vehicle
        VehicleEntity vehicle = vehicleRepository.findByVehicleId(tripRequest.getVehicleId());
        if (vehicle == null) {
            return new BaseResponse(404,null,"Vehicle not found",null,null);
        }
        // Validate time
        if (tripRequest.getArrivalTime().isBefore(tripRequest.getDepartureTime())) {
            return new BaseResponse(400, null, "Arrival time and Departure time are invalid", null, null);
        }

        if (tripRequest.getDepartureTime().isBefore(LocalDateTime.now())) {
            return new BaseResponse(400,null,"Departure time must be longer than current time",null,null);
        }
        // Validate price
        if (tripRequest.getPrice() == null || tripRequest.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return new BaseResponse(400,null,"Ticket prices invalid",null,null);
        }

        // ── Validate & load drivers (bắt buộc có ít nhất 1) ──────────────
        if (tripRequest.getDriverIds() == null || tripRequest.getDriverIds().isEmpty()) {
            return new BaseResponse(400, null, "At least one driver is required", null, null);
        }
        List<EmployeeEntity> drivers = new ArrayList<>();
        for (UUID driverId : tripRequest.getDriverIds()) {
            EmployeeEntity driver = employeeRepository.findByEmployeeId(driverId);
            if (driver == null) {
                return new BaseResponse(404, null, "Driver not found: " + driverId, null, null);
            }
            boolean conflict = tripRepository.existsEmployeeConflict(
                    driverId,
                    tripRequest.getDepartureTime(),
                    tripRequest.getArrivalTime(),
                    List.of(TripStatusEnum.COMPLETED, TripStatusEnum.CANCELLED)
            );
            if (conflict) {
                return new BaseResponse(400, null,
                        "Driver " + driver.getFullName() + " has been assigned to another trip during this time",
                        null, null);
            }
            drivers.add(driver);
        }

        // ── Validate & load assistants (tuỳ chọn) ────────────────────────
        List<EmployeeEntity> assistants = new ArrayList<>();
        if (tripRequest.getAssistantIds() != null) {
            for (UUID assistantId : tripRequest.getAssistantIds()) {
                EmployeeEntity assistant = employeeRepository.findByEmployeeId(assistantId);
                if (assistant == null) {
                    return new BaseResponse(404, null, "Assistant not found: " + assistantId, null, null);
                }
                boolean conflict = tripRepository.existsEmployeeConflict(
                        assistantId,
                        tripRequest.getDepartureTime(),
                        tripRequest.getArrivalTime(),
                        List.of(TripStatusEnum.COMPLETED, TripStatusEnum.CANCELLED)
                );
                if (conflict) {
                    return new BaseResponse(400, null,
                            "Assistant " + assistant.getFullName() + " has been assigned to another trip during this time",
                            null, null);
                }
                assistants.add(assistant);
            }
        }

        // Validate vehicle conflict
        boolean vehicleConflict = tripRepository.existsVehicleConflict(
                vehicle.getVehicleId(),
                tripRequest.getDepartureTime(),
                tripRequest.getArrivalTime(),
                List.of(TripStatusEnum.COMPLETED, TripStatusEnum.CANCELLED)
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
        trip.setPrice(tripRequest.getPrice());
        trip.setStatus(TripStatusEnum.CREATED);
        trip.setRoute(route);
        trip.setVehicle(vehicle);
        trip.setCreatedBy(info.getUsername());
        trip.setCreatedAt(LocalDateTime.now());
        // Gán drivers và assistants qua bảng trung gian
        drivers.forEach(d -> trip.addEmployee(d, AccountRoleEnum.DRIVER));
        assistants.forEach(a -> trip.addEmployee(a, AccountRoleEnum.ASSISTANT));

        tripRepository.save(trip);

        // Save log
        TripLog tripLog = new TripLog();
        tripLog.setChangeBy(info.getUsername());
        tripLog.setChangeAt(LocalDateTime.now());
        tripLog.setStatus(TripStatusEnum.CREATED);
        tripLog.setTrip(trip);
        tripLogRepository.save(tripLog);

        // Create TripSeat
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
        TripResponse response = toResponse(trip);
        return new BaseResponse(200,response,"Create trip successful",null,null);
    }

    @Transactional
    public BaseResponse updateTrip(TripRequest request) {
        UserDetails info = getInfo();

        TripEntity trip = tripRepository.findById(request.getTripId()).orElse(null);
        if (trip == null) {
            return new BaseResponse(404,null,"Trip not found", null, null);
        }

        trip.setStatus(request.getStatus());
        trip.setUpdatedBy(info.getUsername());
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);

        // Save log
        TripLog tripLog = new TripLog();
        tripLog.setChangeBy(info.getUsername());
        tripLog.setChangeAt(LocalDateTime.now());
        tripLog.setStatus(request.getStatus());
        tripLog.setTrip(trip);
        tripLogRepository.save(tripLog);

        TripResponse response = toResponse(trip);
        return new BaseResponse(200,response,"Update trip successful", null, null);
    }

    private UserDetails getInfo() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private TripResponse toResponse(TripEntity trip) {
        return TripResponse.builder()
                .tripId(trip.getTripId())
                .tripCode(trip.getTripCode())
                .departureTime(trip.getDepartureTime())
                .arrivalTime(trip.getArrivalTime())
                .price(trip.getPrice())
                .status(trip.getStatus())
                .fromStation(trip.getRoute().getFromStation().getName())
                .toStation(trip.getRoute().getToStation().getName())
                .licensePlate(trip.getVehicle().getLicensePlate())
                .totalSeat(trip.getVehicle().getTotalSeat())
                .driverNames(trip.getDrivers().stream()
                        .map(EmployeeEntity::getFullName).toList())
                .assistantNames(trip.getAssistants().stream()
                        .map(EmployeeEntity::getFullName).toList())
                .build();
    }
}
