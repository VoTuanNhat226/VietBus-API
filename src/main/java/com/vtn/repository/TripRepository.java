package com.vtn.repository;

import com.vtn.entity.EmployeeEntity;
import com.vtn.entity.TripEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TripRepository extends JpaRepository<TripEntity, UUID> {

    @Query("""
        SELECT COUNT(t) > 0
        FROM TripEntity t
        WHERE t.driver.employeeId = :driverId
          AND t.route.routeId = :routeId
          AND t.departureTime < :arrivalTime
          AND t.arrivalTime > :departureTime
          AND t.status NOT IN ('COMPLETED', 'CANCELLED')
    """)
    boolean existsDriverConflict(
            @Param("driverId") UUID driverId,
            @Param("routeId") UUID routeId,
            @Param("departureTime") LocalDateTime departureTime,
            @Param("arrivalTime") LocalDateTime arrivalTime
    );

    @Query("""
        SELECT COUNT(t) > 0
        FROM TripEntity t
        WHERE t.vehicle.vehicleId = :vehicleId
          AND t.departureTime < :arrivalTime
          AND t.arrivalTime > :departureTime
          AND t.status NOT IN ('COMPLETED', 'CANCELLED')
    """)
    boolean existsVehicleConflict(
            @Param("vehicleId") UUID vehicleId,
            @Param("departureTime") LocalDateTime departureTime,
            @Param("arrivalTime") LocalDateTime arrivalTime
    );

    @Query("""
    SELECT t
    FROM TripEntity t
    WHERE (:fromStationId IS NULL OR t.route.fromStation.stationId = :fromStationId)
        AND (:toStationId IS NULL OR t.route.toStation.stationId = :toStationId)
        AND (:driverId IS NULL OR t.driver.employeeId = :driverId)
        AND (:vehicleId IS NULL OR t.vehicle.vehicleId = :vehicleId)
        AND (:status IS NULL OR t.status LIKE %:status%)
    """)
    List<TripEntity> getAllByCondition(
            @Param("fromStationId") UUID fromStationId,
            @Param("toStationId") UUID toStationId,
            @Param("driverId") UUID driverId,
            @Param("vehicleId") UUID vehicleId,
            @Param("status") String status
    );
}
