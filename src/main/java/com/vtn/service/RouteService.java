package com.vtn.service;

import com.vtn.dto.request.RouteRequest;
import com.vtn.entity.RouteEntity;
import com.vtn.entity.StationEntity;
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

    public BaseResponse getAllRoutes(RouteRequest request) {
        List<RouteEntity> routes = routeRepository.getAllByCondition(
                    request.getFromStationId(),
                    request.getToStationId(),
                    request.getDistanceKm(),
                    request.getCreatedBy(),
                    request.getUpdatedBy(),
                    request.getActive()
            );

        return new BaseResponse(200, routes, "Get all routes successful", null, null);
    }

    public BaseResponse getAllRoutesActive() {
        List<RouteEntity> routes = routeRepository.findAllRoutesActive();
        return new BaseResponse(200, routes, "Get all routes active successful",null,null);
    }

    public BaseResponse createRoute(RouteRequest routeRequest) {
        UserDetails info = getInfo();

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

        StationEntity fromStation = stationRepository.findByStationId(routeRequest.getFromStationId());
        StationEntity toStation = stationRepository.findByStationId(routeRequest.getToStationId());

        RouteEntity route = new RouteEntity();
        route.setFromStation(fromStation);
        route.setToStation(toStation);
        route.setDistanceKm(routeRequest.getDistanceKm());
        route.setActive(routeRequest.getActive());
        route.setCreatedBy(info.getUsername());
        route.setCreatedAt(LocalDateTime.now());
        routeRepository.save(route);

        return new BaseResponse(201, route, "Create route successful", null, null);
    }

    public BaseResponse updateRoute(RouteRequest routeRequest) {
        UserDetails info = getInfo();

        RouteEntity route = routeRepository.findByRouteId(routeRequest.getRouteId());
        if(route != null) {
            route.setActive(routeRequest.getActive());
            route.setUpdatedBy(info.getUsername());
            route.setUpdatedAt(LocalDateTime.now());
            routeRepository.save(route);
        }

        return new BaseResponse(200, route, "Update route successful", null, null);
    }

    public BaseResponse deleteRoute(RouteRequest routeRequest) {
        UserDetails info = getInfo();
        boolean isAdmin = isAdmin(info);

        if(!isAdmin) {
            return new BaseResponse(403, null, "You don't has permission", null, null);
        } else {
            RouteEntity route = routeRepository.findByRouteId(routeRequest.getRouteId());
            if(route == null) {
                return new BaseResponse(404, null, "Not found route", null, null);
            } else {
                routeRepository.delete(route);
                return new BaseResponse(204, route, "Delete route successful", null, null);
            }
        }
    }

    private boolean isAdmin(UserDetails info) {
        return info.getAuthorities()
                .stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private UserDetails getInfo() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
