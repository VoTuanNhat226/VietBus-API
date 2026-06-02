package com.vtn.service;

import com.vtn.dto.request.TripSeatRequest;
import com.vtn.dto.response.TripSeatResponse;
import com.vtn.entity.TripSeatEntity;
import com.vtn.enumdef.TripSeatStatusEnum;
import com.vtn.repository.TripSeatRepository;
import com.vtn.utils.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TripSeatService {
    private final TripSeatRepository tripSeatRepository;

    @Autowired
    public TripSeatService(TripSeatRepository tripSeatRepository) {
        this.tripSeatRepository = tripSeatRepository;
    }

    public BaseResponse countTripSeatSoldByTripId(TripSeatRequest request) {
        if (request.getTripId() == null) {
            return new BaseResponse(400, null, "TripId is required", null, null);
        }
        Integer count = tripSeatRepository.countTripSeatSoldByTripId(request.getTripId(), List.of(TripSeatStatusEnum.SOLD, TripSeatStatusEnum.HOLD));
        return new BaseResponse(200, count, "Get quantity trip seat sold successful",null,null);
    }

    public BaseResponse getAllTripSeatsByTripId(TripSeatRequest request) {
        if (request.getTripId() == null) {
            return new BaseResponse(400, null, "TripId is required", null, null);
        }
        List<TripSeatEntity> tripSeats = tripSeatRepository.findAllTripSeatsByTripId(request.getTripId());
        return new BaseResponse(200, tripSeats, "Get all trip seats successful",null,null);

    }

    public BaseResponse getAllTripSeatAvailableByTripId(TripSeatRequest request) {
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
        return new BaseResponse(200, result, "Get all trip seats can sell successful",null,null);
    }
}
