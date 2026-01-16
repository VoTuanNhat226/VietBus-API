package com.vtn.repository;

import com.vtn.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface RouteRepository extends JpaRepository<RouteEntity, Integer> {

    RouteEntity findByRouteId(UUID routeId);

    @Query("""
        SELECT r 
        FROM RouteEntity r
        WHERE r.fromStation = :fromStation
        AND r.toStation = :toStation    
    """)
    RouteEntity findByFromStationAndToStation(String fromStation, String toStation);
}
