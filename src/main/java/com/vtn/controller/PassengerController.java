package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.PassengerRequest.CreatePassengerRequest;
import com.vtn.dto.response.PassengerResponse;
import com.vtn.service.PassengerService;
import com.vtn.utils.BasePageRequest;
import com.vtn.utils.BaseResponeNew;
import com.vtn.utils.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PassengerController {
    private final PassengerService passengerService;

    @Autowired
    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @PostMapping(value = APIConstants.API_CREATE_PASSENGER)
    public ResponseEntity<BaseResponse> createPassenger(@RequestBody CreatePassengerRequest request) {
        long begin = System.currentTimeMillis();
        BaseResponse response = passengerService.createAPassenger(request);
        response.setTook(System.currentTimeMillis() - begin);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(APIConstants.API_GET_PASSENGER)
    public ResponseEntity<?> getPassenger(@Valid @RequestBody BasePageRequest request) {
        return ResponseEntity.ok(passengerService.getListPassenger(request));
    }
}
