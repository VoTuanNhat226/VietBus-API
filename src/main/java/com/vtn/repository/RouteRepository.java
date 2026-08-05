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
        WHERE r.fromStation.stationId = :fromStationId
            AND r.toStation.stationId = :toStationId
    """)
    RouteEntity findByFromStationAndToStation(UUID fromStationId, UUID toStationId);

    @Query("""
        SELECT r
        FROM RouteEntity r
        WHERE (:fromStationId IS NULL OR r.fromStation.stationId = :fromStationId)
            AND (:toStationId IS NULL OR r.toStation.stationId = :toStationId)
            AND (:distanceKm IS NULL OR r.distanceKm = :distanceKm)
            AND (:createdBy IS NULL OR r.createdBy LIKE :createdBy%)
            AND (:updatedBy IS NULL OR r.updatedBy LIKE :updatedBy%)
            AND (:active IS NULL OR r.active = :active)
    """)
    List<RouteEntity> getAllByCondition(
            @Param("fromStationId") UUID fromStationId,
            @Param("toStationId") UUID toStationId,
            @Param("distanceKm") Integer distanceKm,
            @Param("createdBy") String createdBy,
            @Param("updatedBy") String updatedBy,
            @Param("active") Boolean active
    );

    @Query("""
        SELECT r
        FROM RouteEntity r
        WHERE r.active = true
    """)
    List<RouteEntity> findAllRoutesActive();
}
