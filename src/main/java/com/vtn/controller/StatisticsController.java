package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.StatisticsRequest;
import com.vtn.service.StatisticsService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatisticsController {
    private final StatisticsService statisticsService;

    @Autowired
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @PostMapping(value = APIConstants.API_GET_REVENUE_BY_MONTH)
    public ResponseEntity<BaseResponse> getRevenueByMonth(@RequestBody StatisticsRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = statisticsService.getRevenueByMonth(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = APIConstants.API_GET_TOTAL_TICKET_BY_MONTH)
    public ResponseEntity<BaseResponse> getAllTicketByMonth(@RequestBody StatisticsRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = statisticsService.countTicketByMonth(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = APIConstants.API_GET_TOTAL_TRIP_BY_MONTH)
    public ResponseEntity<BaseResponse> getAllTripByMonth(@RequestBody StatisticsRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = statisticsService.countTripByMonth(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
