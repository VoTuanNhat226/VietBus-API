package com.vtn.service;

import com.vtn.dto.request.RouteRequest;
import com.vtn.entity.RouteEntity;
import com.vtn.entity.StationEntity;
import com.vtn.entity.TripEntity;
import com.vtn.repository.RouteRepository;
import com.vtn.repository.StationRepository;
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
    private final StationRepository stationRepository;

    @Autowired
    public RouteService(
            RouteRepository routeRepository,
            StationRepository stationRepository) {
        this.routeRepository = routeRepository;
        this.stationRepository = stationRepository;
    }

    private boolean isAllParametersNull(RouteRequest request) {
        return ((request.getFromStationId() == null) &&
                (request.getToStationId() == null) &&
                (request.getDistanceKm() == null) &&
                (request.getDurationMinutes() == null) &&
                (request.getCreatedBy() == null) &&
                (request.getUpdatedBy() == null) &&
                (request.getActive() == null));
    }

    public BaseResponse getAllRoutes(RouteRequest request) {
        List<RouteEntity> routes;
        if (isAllParametersNull(request)) {
            routes = routeRepository.findAll();
        } else {
            routes = routeRepository.getAllByCondition(
                    request.getFromStationId(),
                    request.getToStationId(),
                    request.getDistanceKm(),
                    request.getDurationMinutes(),
                    request.getCreatedBy(),
                    request.getUpdatedBy(),
                    request.getActive()
            );
        }
        return new BaseResponse(200, routes, "Get all routes successfully", null, null);
    }

    public BaseResponse getAllRoutesActive() {
        try {
            List<RouteEntity> routes = routeRepository.findAllRoutesActive();
            return new BaseResponse(200, routes, "Get all routes active successfully",null,null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BaseResponse createRoute(RouteRequest routeRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            RouteEntity routeEntity = routeRepository.findByFromStationAndToStation(routeRequest.getFromStationId(), routeRequest.getToStationId());
            if(routeEntity != null) {
                return new BaseResponse(400, null,"Tuyến xe đã tồn tại", null,null);
            }
            if(routeRequest.getFromStationId().equals(routeRequest.getToStationId())) {
                return new BaseResponse(400, null,"Điểm đi trùng điểm đến", null,null);
            }
            if(routeRequest.getDistanceKm() == 0) {
                return new BaseResponse(400, null,"Khoảng cách phải lớn hơn 0", null,null);
            }
            if(routeRequest.getDurationMinutes() == 0) {
                return new BaseResponse(400, null,"Thời gian phải lớn hơn 0", null,null);
            }

            StationEntity fromStaion = stationRepository.findByStationId(routeRequest.getFromStationId());
            StationEntity toStaion = stationRepository.findByStationId(routeRequest.getToStationId());

            RouteEntity route = new RouteEntity();
            route.setFromStation(fromStaion);
            route.setToStation(toStaion);
            route.setDistanceKm(routeRequest.getDistanceKm());
            route.setDurationMinutes(routeRequest.getDurationMinutes());
            route.setActive(routeRequest.getActive());
            route.setCreatedBy(info.getUsername());
            route.setCreatedAt(LocalDateTime.now());
            routeRepository.save(route);
            return new BaseResponse(201, route, "Create route successfully", "No error", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BaseResponse updateRoute(RouteRequest routeRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            RouteEntity route = routeRepository.findByRouteId(routeRequest.getRouteId());
            if(route != null) {
                route.setActive(routeRequest.getActive());
                route.setUpdatedBy(info.getUsername());
                route.setUpdatedAt(LocalDateTime.now());
                routeRepository.save(route);
            }
            return new BaseResponse(200, route, "Update route successfully", "No error", null);
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
