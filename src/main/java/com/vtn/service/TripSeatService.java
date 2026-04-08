package com.vtn.service;

import com.vtn.dto.request.TripRequest;
import com.vtn.dto.request.TripSeatRequest;
import com.vtn.dto.response.TripSeatResponse;
import com.vtn.entity.TripSeatEntity;
import com.vtn.repository.TripSeatRepository;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TripSeatService {
    private final TripSeatRepository tripSeatRepository;

    @Autowired
    public TripSeatService(TripSeatRepository tripSeatRepository) {
        this.tripSeatRepository = tripSeatRepository;
    }

    public BaseResponse countTripSeatSoldByTripId(TripSeatRequest request) {
        try {
            Integer count = tripSeatRepository.countTripSeatSoldByTripId(request.getTripId());
            return new BaseResponse(200, count, "Get trip seat sold successfully",null,null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BaseResponse getAllTripSeatsByTripId(TripSeatRequest request) {
        try {
            List<TripSeatEntity> tripSeats = tripSeatRepository.findAllTripSeatsByTripId(request.getTripId());
            return new BaseResponse(200, tripSeats, "Get all trip seats successfully",null,null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BaseResponse getAllTripSeatAvailableByTripId(TripSeatRequest request) {
        try {
            List<TripSeatResponse> result  = tripSeatRepository.findAllTripSeatAvailableByTripId(request.getTripId())
                    .stream()
                    .map(ts -> new TripSeatResponse(
                            //TripSeat
                            ts.getId(),
                            ts.getStatus(),
                            //Seat
                            ts.getSeat().getSeatId(),
                            ts.getSeat().getSeatNumber(),
                            ts.getSeat().getFloor(),
                            ts.getSeat().getSeatRow(),
                            ts.getSeat().getSeatColumn(),
                            //Trip
                            ts.getTrip().getTripId(),
                            ts.getTrip().getTripCode(),
                            ts.getTrip().getRoute().getFromStation().getName(),
                            ts.getTrip().getRoute().getToStation().getName(),
                            ts.getTrip().getDepartureTime(),
                            ts.getTrip().getArrivalTime(),
                            ts.getTrip().getPrice(),
                            ts.getTrip().getStatus()
                    ))
                    .toList();
            return new BaseResponse(200, result, "Get all trip seats can sell successfully",null,null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
