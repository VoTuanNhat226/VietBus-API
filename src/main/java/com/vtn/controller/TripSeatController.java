package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.TripRequest;
import com.vtn.dto.request.TripSeatRequest;
import com.vtn.service.TripSeatService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TripSeatController {
    private final TripSeatService tripSeatService;

    @Autowired
    public TripSeatController(TripSeatService tripSeatService) {
        this.tripSeatService = tripSeatService;
    }

    @PostMapping(value = APIConstants.API_GET_ALL_TRIP_SEAT_BY_TRIP_ID)
    public ResponseEntity<BaseResponse> getAllTripSeatByTripId(@RequestBody TripSeatRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = tripSeatService.getAllTripSeatsByTripId(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = APIConstants.API_GET_ALL_TRIP_SEAT_AVAILABLE_BY_TRIP_ID)
    public ResponseEntity<BaseResponse> getAllTripSeatAvailableByTripId(@RequestBody TripSeatRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = tripSeatService.getAllTripSeatAvailableByTripId(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = APIConstants.API_COUNT_TRIP_SEAT_SOLD_BY_TRIP_ID)
    public ResponseEntity<BaseResponse> countTripSeatSoldByTripId(@RequestBody TripSeatRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = tripSeatService.countTripSeatSoldByTripId(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }
}
