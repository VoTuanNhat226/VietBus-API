package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.VehicleRequest;
import com.vtn.service.VehicleService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VehicleController {
    private final VehicleService vehicleService;

    @Autowired
    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping(value = APIConstants.API_GET_ALL_VEHICLES)
    public ResponseEntity<BaseResponse> getAll() {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = vehicleService.getAllBuses();
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = APIConstants.API_GET_VEHICLE_BY_VEHICLE_ID)
    public ResponseEntity<BaseResponse> getBusById(@RequestBody VehicleRequest vehicleRequest) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = vehicleService.getVehicleById(vehicleRequest);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = APIConstants.API_CREATE_VEHICLE)
    public ResponseEntity<BaseResponse> create(@RequestBody VehicleRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = vehicleService.createBus(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return  ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }

    @PostMapping(value = APIConstants.API_UPDATE_VEHICLE)
    public ResponseEntity<BaseResponse> update(@RequestBody VehicleRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = vehicleService.updateBus(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return  ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }

    @PostMapping(value = APIConstants.API_DELETE_VEHICLE)
    public ResponseEntity<BaseResponse> delete(@RequestBody VehicleRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = vehicleService.deleteBus(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }
}
