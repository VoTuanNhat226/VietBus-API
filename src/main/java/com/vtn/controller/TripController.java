package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.TripRequest;
import com.vtn.service.TripService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TripController {
    private final TripService tripService;
    @Autowired
    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping(value = APIConstants.API_GET_ALL_TRIP)
    public ResponseEntity<BaseResponse> getAllTrip(@RequestBody TripRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = tripService.getAllTrips(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = APIConstants.API_GET_ALL_TRIP_OPEN_FOR_BOOKING)
    public ResponseEntity<BaseResponse> getAllTripOpenBooking() {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = tripService.getAllTripOpenBooking();
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = APIConstants.API_GET_TRIP_BY_TRIP_ID)
    public ResponseEntity<BaseResponse> getTripByTripId(@RequestBody TripRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = tripService.getTripByTripId(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = APIConstants.API_CREATE_TRIP)
    public ResponseEntity<BaseResponse> create(@RequestBody TripRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = tripService.createTrip(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }

    @PostMapping(value = APIConstants.API_UPDATE_TRIP)
    public ResponseEntity<BaseResponse> update(@RequestBody TripRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = tripService.updateTrip(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }
}
