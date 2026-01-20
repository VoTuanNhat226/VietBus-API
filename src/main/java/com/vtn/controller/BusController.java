package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.BusRequest;
import com.vtn.service.BusService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BusController {
    private final BusService busService;

    @Autowired
    public BusController(BusService busService) {
        this.busService = busService;
    }

    @PostMapping(value = APIConstants.API_GET_ALL_BUSES)
    public ResponseEntity<BaseResponse> getAll() {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = busService.getAllBuses();
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = APIConstants.API_CREATE_BUS)
    public ResponseEntity<BaseResponse> create(@RequestBody BusRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = busService.createBus(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return  ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }

    @PostMapping(value = APIConstants.API_UPDATE_BUS)
    public ResponseEntity<BaseResponse> update(@RequestBody BusRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = busService.updateBus(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return  ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }

    @PostMapping(value = APIConstants.API_DELETE_BUS)
    public ResponseEntity<BaseResponse> delete(@RequestBody BusRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = busService.deleteBus(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }
}
