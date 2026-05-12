package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.AccountRequest;
import com.vtn.dto.request.PassengerRequest;
import com.vtn.dto.request.PassengerSearchRequest;
import com.vtn.service.PassengerService;
import com.vtn.utils.BasePageRequest;
import com.vtn.utils.BaseResponse;
import jakarta.validation.Valid;
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

    @PostMapping(value = APIConstants.API_GET_ALL_PASSENGER)
    public ResponseEntity<BaseResponse> getAll() {
        long begin = System.currentTimeMillis();
        BaseResponse response = passengerService.getAllPassenger();
        response.setTook(System.currentTimeMillis() - begin);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = APIConstants.API_CREATE_PASSENGER)
    public ResponseEntity<BaseResponse> createPassenger(@RequestBody PassengerRequest request) {
        long begin = System.currentTimeMillis();
        BaseResponse response = passengerService.createPassenger(request);
        response.setTook(System.currentTimeMillis() - begin);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(APIConstants.API_GET_PASSENGER)
    public ResponseEntity<?> getPassenger(@Valid @RequestBody BasePageRequest request) {
        return ResponseEntity.ok(passengerService.getListPassenger(request));
    }

    @PostMapping(APIConstants.API_UPDATE_PASSENGER)
    public ResponseEntity<?> updatePassenger(@Valid @RequestBody PassengerRequest request) {
        return ResponseEntity.ok(passengerService.updatePassenger(request));
    }

    @PostMapping(APIConstants.API_DELETE_PASSENGER)
    public ResponseEntity<?> deletePassenger(@Valid @RequestBody PassengerRequest request) {
        return ResponseEntity.ok(passengerService.deletePassenger(request));
    }

    @PostMapping(APIConstants.API_SEARCH_PASSENGER)
    public ResponseEntity<?> searchPassenger(@Valid @RequestBody PassengerSearchRequest request) {
        return ResponseEntity.ok(passengerService.searchPassenger(request));
    }
}
