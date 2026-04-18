package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.AccountRequest;
import com.vtn.dto.request.PassengerRequest;
import com.vtn.service.PassengerService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PassengerController {
    private final PassengerService passengerService;

    @Autowired
    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @PostMapping(value = APIConstants.API_CREATE_PASSENGER)
    public ResponseEntity<BaseResponse> createPassenger(@RequestBody PassengerRequest request) {
        long begin = System.currentTimeMillis();
        BaseResponse response = passengerService.createAPassenger(request);
        response.setTook(System.currentTimeMillis() - begin);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}`
