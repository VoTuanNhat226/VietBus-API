package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.SeatRequest;
import com.vtn.dto.request.VehicleRequest;
import com.vtn.service.SeatService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeatController {
    private final SeatService seatService;

    @Autowired
    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping(value = APIConstants.API_GET_SEAT_BY_VEHICLE_ID)
    public ResponseEntity<BaseResponse> getSeatByVehicleId(@RequestBody SeatRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = seatService.getSeatByVehicleId(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
