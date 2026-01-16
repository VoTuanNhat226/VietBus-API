package com.vtn.service;

import com.vtn.dto.request.RouteRequest;
import com.vtn.entity.RouteEntity;
import com.vtn.repository.RouteRepository;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RouteService {
    private final RouteRepository routeRepository;

    @Autowired
    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public BaseResponse getAllRoutes() {
        try {
            List<RouteEntity> routes = routeRepository.findAll();
            return new BaseResponse(200, routes, "Get all routes successfully", "Get all routes successfully",null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BaseResponse createRoute(RouteRequest routeRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            RouteEntity routeEntity = routeRepository.findByFromStationAndToStation(routeRequest.getFromStation(), routeRequest.getToStation());
            if(routeEntity != null) {
                return new BaseResponse(400, null,"Route already exists", "Route already exists",null);
            }
            if(routeRequest.getFromStation().equals(routeRequest.getToStation())) {
                return new BaseResponse(400, null,"From Station equal To Station", "From Station equal To Station",null);
            }
            if(routeRequest.getDistanceKm() == 0) {
                return new BaseResponse(400, null,"Distance Km must be greater than 0", "Distance Km must be greater than 0",null);
            }
            if(routeRequest.getDurationMinutes() == 0) {
                return new BaseResponse(400, null,"Duration minutes must be greater than 0", "Duration minutes must be greater than 0",null);
            }
            RouteEntity route = new RouteEntity();
            route.setFromStation(routeRequest.getFromStation());
            route.setToStation(routeRequest.getToStation());
            route.setDistanceKm(routeRequest.getDistanceKm());
            route.setDurationMinutes(routeRequest.getDurationMinutes());
            route.setActive(true);
            route.setCreated_by(info.getUsername());
            route.setCreated_at(LocalDateTime.now());
            routeRepository.save(route);
            return new BaseResponse(201, route, "Create route successfully", "No error", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BaseResponse deleteRoute(RouteRequest routeRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = info.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
        try {
            if("ROLE_ADMIN".equals(role)) {
                RouteEntity route = routeRepository.findByRouteId(routeRequest.getRouteId());
                if(route == null) {
                    return new BaseResponse(404, null, "Not found route", "Not found route", null);
                } else {
                    routeRepository.delete(route);
                    return new BaseResponse(204, route, "Delete route successfully", "No error", null);
                }
            } else {
                return new BaseResponse(403, null, "You don't has permission", "No error", null);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
