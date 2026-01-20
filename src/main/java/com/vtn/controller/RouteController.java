package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.RouteRequest;
import com.vtn.service.RouteService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.RouteMatcher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RouteController {
    private final RouteService routeService;

    @Autowired
    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping(value = APIConstants.API_GET_ALL_ROUTE)
    public ResponseEntity<BaseResponse> getAll(@RequestBody RouteRequest request) {
        long begin = System.currentTimeMillis();
        BaseResponse response = routeService.getAllRoutes(request);
        response.setTook(System.currentTimeMillis() - begin);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = APIConstants.API_CREATE_ROUTE)
    public ResponseEntity<BaseResponse> create(@RequestBody RouteRequest request) {
        long begin = System.currentTimeMillis();
        BaseResponse response = routeService.createRoute(request);
        response.setTook(System.currentTimeMillis() - begin);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(value = APIConstants.API_DELETE_ROUTE)
    public ResponseEntity<BaseResponse> delete(@RequestBody RouteRequest request) {
        long begin = System.currentTimeMillis();
        BaseResponse response = routeService.deleteRoute(request);
        response.setTook(System.currentTimeMillis() - begin);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }
}
