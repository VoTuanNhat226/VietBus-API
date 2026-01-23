package com.vtn.repository;

import com.vtn.entity.EmployeeEntity;
import com.vtn.entity.TripEntity;
import com.vtn.entity.TripSeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TripRepository extends JpaRepository<TripEntity, UUID> {

    boolean existsByTripCode(String tripCode);

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
        AND (:tripCode IS NULL OR t.tripCode = :tripCode)
    """)
    List<TripEntity> getAllByCondition(
            @Param("fromStationId") UUID fromStationId,
            @Param("toStationId") UUID toStationId,
            @Param("driverId") UUID driverId,
            @Param("vehicleId") UUID vehicleId,
            @Param("status") String status,
            @Param("tripCode") String tripCode
    );

    @Query("""
        SELECT COUNT(t)
        FROM TripSeatEntity t
        WHERE t.trip.tripId = :tripId
            AND t.status = 'SOLD'
    """)
    Integer countTripSeatSoldByTripId(@Param("tripId") UUID tripId);

    @Query("""
        SELECT t
        FROM TripSeatEntity t
        WHERE t.trip.tripId = :tripId
    """)
    List<TripSeatEntity> findAllTripSeatsByTripId(UUID tripId);
}
