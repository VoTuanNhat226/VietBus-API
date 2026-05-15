package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.TripRequest;
import com.vtn.service.TripHistoryService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TripHistoryController {
    private final TripHistoryService tripHistoryService;

    @Autowired
    public TripHistoryController(TripHistoryService tripHistoryService) {
        this.tripHistoryService = tripHistoryService;
    }

    @PostMapping(value = APIConstants.API_GET_TRIP_HISTORY_BY_TRIP_ID)
    public ResponseEntity<BaseResponse> getByTripId(@RequestBody TripRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = tripHistoryService.getByTripId(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
