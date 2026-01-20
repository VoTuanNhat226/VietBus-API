package com.vtn.service;

import com.vtn.dto.request.TripRequest;
import com.vtn.entity.*;
import com.vtn.repository.*;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public BaseResponse getAllTrips() {
        try {
            List<TripEntity> trips = tripRepository.findAll();
            return new BaseResponse(200, trips, "Get all trips successfully","Get all trips successfully",null);
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
                return new BaseResponse(400,null,"Route not found","Route not found",null);
            }
            VehicleEntity bus = vehicleRepository.findByVehicleId(tripRequest.getBusId());
            if (bus == null) {
                return new BaseResponse(400,null,"Bus not found","Bus not found",null);
            }
            EmployeeEntity driver = employeeRepository.findByEmployeeId(tripRequest.getDriverId());
            if (driver == null) {
                return new BaseResponse(400,null,"Driver not found","Employee not found",null);
            }
            if (tripRequest.getArrivalTime().isBefore(tripRequest.getDepartureTime())) {
                return new BaseResponse(400, null, "Invalid trip time", "Invalid trip time", null);
            }
            TripEntity trip = new TripEntity();
            trip.setDepartureTime(tripRequest.getDepartureTime());
            trip.setArrivalTime(tripRequest.getArrivalTime());
            trip.setPrice(tripRequest.getPrice());
            trip.setStatus(tripRequest.getStatus());
            trip.setRoute(route);
            trip.setBus(bus);
            trip.setDriver(driver);
            trip.setCreated_by(info.getUsername());
            trip.setCreated_at(LocalDateTime.now());
            tripRepository.save(trip);

            List<SeatEntity> seats = seatRepository.findByVehicleId(bus.getVehicleId());

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
