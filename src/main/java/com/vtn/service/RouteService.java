package com.vtn.service;

import com.vtn.dto.request.RouteRequest;
import com.vtn.entity.RouteEntity;
import com.vtn.entity.StationEntity;
import com.vtn.repository.RouteRepository;
import com.vtn.repository.StationRepository;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
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

        BaseResponse existsError = validateRouteDoesNotExist(routeRequest);
        if (existsError != null) {
            return existsError;
        }

        BaseResponse stationsError = validateStationsDifferent(routeRequest);
        if (stationsError != null) {
            return stationsError;
        }

        BaseResponse distanceError = validateDistancePositive(routeRequest);
        if (distanceError != null) {
            return distanceError;
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
        BaseResponse routeError = validateRouteExists(route);
        if (routeError != null) {
            return routeError;
        }

        route.setActive(routeRequest.getActive());
        route.setUpdatedBy(info.getUsername());
        route.setUpdatedAt(LocalDateTime.now());
        routeRepository.save(route);

        return new BaseResponse(200, route, "Update route successful", null, null);
    }

    public BaseResponse deleteRoute(RouteRequest routeRequest) {
        UserDetails info = getInfo();

        BaseResponse permissionError = validateIsAdmin(info);
        if (permissionError != null) {
            return permissionError;
        }

        RouteEntity route = routeRepository.findByRouteId(routeRequest.getRouteId());
        BaseResponse routeError = validateRouteExists(route);
        if (routeError != null) {
            return routeError;
        }

        routeRepository.delete(route);
        return new BaseResponse(204, route, "Delete route successful", null, null);
    }

    // ------------------ validate ------------------
    private BaseResponse validateRouteDoesNotExist(RouteRequest routeRequest) {
        RouteEntity routeEntity = routeRepository.findByFromStationAndToStation(routeRequest.getFromStationId(), routeRequest.getToStationId());
        if (routeEntity != null) {
            return new BaseResponse(409, null, "Tuyến xe đã tồn tại", null, null);
        }
        return null;
    }

    private BaseResponse validateStationsDifferent(RouteRequest routeRequest) {
        if (routeRequest.getFromStationId().equals(routeRequest.getToStationId())) {
            return new BaseResponse(400, null, "Điểm đi trùng điểm đến", null, null);
        }
        return null;
    }

    private BaseResponse validateDistancePositive(RouteRequest routeRequest) {
        if (routeRequest.getDistanceKm() == 0) {
            return new BaseResponse(400, null, "Khoảng cách phải lớn hơn 0", null, null);
        }
        return null;
    }

    private BaseResponse validateRouteExists(RouteEntity route) {
        if (route == null) {
            return new BaseResponse(404, null, "Route not found", null, null);
        }
        return null;
    }

    private BaseResponse validateIsAdmin(UserDetails info) {
        if (!isAdmin(info)) {
            return new BaseResponse(403, null, "You don't has permission", null, null);
        }
        return null;
    }

    // ------------------ helper ------------------
    private boolean isAdmin(UserDetails info) {
        return info.getAuthorities()
                .stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private UserDetails getInfo() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
