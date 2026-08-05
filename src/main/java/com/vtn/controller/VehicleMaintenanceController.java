package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.VehicleMaintenanceRequest;
import com.vtn.service.VehicleMaintenanceService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VehicleMaintenanceController {
    private final VehicleMaintenanceService vehicleMaintenanceService;

    @Autowired
    public VehicleMaintenanceController(VehicleMaintenanceService vehicleMaintenanceService) {
        this.vehicleMaintenanceService = vehicleMaintenanceService;
    }

    @PostMapping(value = APIConstants.API_GET_VEHICLE_MAINTENANCE_BY_VEHICLE_ID)
    public ResponseEntity<BaseResponse> getByVehicleId(@RequestBody VehicleMaintenanceRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = vehicleMaintenanceService.getByVehicleId(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }

    @PostMapping(value = APIConstants.API_CREATE_VEHICLE_MAINTENANCE)
    public ResponseEntity<BaseResponse> create(@RequestBody VehicleMaintenanceRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = vehicleMaintenanceService.createMaintenance(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }

    @PostMapping(value = APIConstants.API_UPDATE_VEHICLE_MAINTENANCE)
    public ResponseEntity<BaseResponse> update(@RequestBody VehicleMaintenanceRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = vehicleMaintenanceService.updateMaintenance(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }
}
