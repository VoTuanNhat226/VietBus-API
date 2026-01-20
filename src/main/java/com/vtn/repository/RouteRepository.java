package com.vtn.repository;

import com.vtn.entity.EmployeeEntity;
import com.vtn.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("""
    SELECT r
    FROM RouteEntity r
    WHERE (:fromStation IS NULL OR r.fromStation LIKE %:fromStation%)
        AND (:toStation IS NULL OR r.toStation LIKE %:toStation%)
        AND (:distanceKm IS NULL OR r.distanceKm = :distanceKm)
        AND (:durationMinutes IS NULL OR r.durationMinutes = :durationMinutes)
        AND (:createdBy IS NULL OR r.createdBy LIKE %:createdBy%)
        AND (:updatedBy IS NULL OR r.updatedBy LIKE :updatedBy%)
        AND (:active IS NULL OR r.active = :active)
    """)
    List<RouteEntity> getAllByCondition(
            @Param("fromStation") String fromStation,
            @Param("toStation") String toStation,
            @Param("distanceKm") Integer distanceKm,
            @Param("durationMinutes") Integer durationMinutes,
            @Param("createdBy") String createdBy,
            @Param("updatedBy") String updatedBy,
            @Param("active") Boolean active
    );
}
