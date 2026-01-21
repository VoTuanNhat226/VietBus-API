package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.service.StationService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StationController {
    private final StationService stationService;

    @Autowired
    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping(value = APIConstants.API_GET_ALL_STATION)
    public ResponseEntity<BaseResponse> getAll() {
        long begin = System.currentTimeMillis();
        BaseResponse response = stationService.getAllStations();
        response.setTook(System.currentTimeMillis() - begin);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
