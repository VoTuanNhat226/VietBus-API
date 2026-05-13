package com.vtn.repository;

import com.vtn.entity.EmployeeEntity;
import com.vtn.entity.TripEntity;
import com.vtn.entity.TripSeatEntity;
import com.vtn.enumdef.TripStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TripRepository extends JpaRepository<TripEntity, UUID> {
    boolean existsByTripCode(String tripCode);
    TripEntity findByTripCode(String tripCode);

    @Query("""
        SELECT COUNT(te) > 0
        FROM TripEmployeeEntity te
        WHERE te.employee.employeeId = :employeeId
            AND te.trip.departureTime < :arrivalTime
            AND te.trip.arrivalTime > :departureTime
            AND te.trip.status NOT IN (:statuses)
    """)
    boolean existsEmployeeConflict(
            @Param("employeeId") UUID employeeId,
            @Param("departureTime") LocalDateTime departureTime,
            @Param("arrivalTime") LocalDateTime arrivalTime,
            @Param("statuses") List<TripStatusEnum> statuses
    );

    @Query("""
        SELECT COUNT(t) > 0
        FROM TripEntity t
        WHERE t.vehicle.vehicleId = :vehicleId
            AND t.departureTime < :arrivalTime
            AND t.arrivalTime > :departureTime
            AND t.status NOT IN (:statuses)
    """)
    boolean existsVehicleConflict(
            @Param("vehicleId") UUID vehicleId,
            @Param("departureTime") LocalDateTime departureTime,
            @Param("arrivalTime") LocalDateTime arrivalTime,
            @Param("statuses") List<TripStatusEnum> statuses
    );

    @Query("""
        SELECT DISTINCT t
        FROM TripEntity t
        JOIN TripEmployeeEntity te ON t.tripId = te.trip.tripId
        WHERE (:fromStationId IS NULL OR t.route.fromStation.stationId = :fromStationId)
            AND (:toStationId IS NULL OR t.route.toStation.stationId = :toStationId)
            AND (:vehicleId IS NULL OR t.vehicle.vehicleId = :vehicleId)
            AND (:status IS NULL OR t.status = :status)
            AND (:tripCode IS NULL OR t.tripCode LIKE %:tripCode%)
            AND (:driverId IS NULL OR te.employee.employeeId = :driverId)
    """)
    List<TripEntity> getAllByCondition(
            @Param("fromStationId") UUID fromStationId,
            @Param("toStationId") UUID toStationId,
            @Param("vehicleId") UUID vehicleId,
            @Param("status") TripStatusEnum status,
            @Param("tripCode") String tripCode,
            @Param("driverId") UUID driverId
    );

    @Query("""
        SELECT t
        FROM TripEntity t
        WHERE t.status = :status
    """)
    List<TripEntity> getAllTripByStatus(@Param("status") TripStatusEnum status);

    @Query("""
        SELECT COUNT(t)
        FROM TripEntity t
        WHERE t.departureTime >= :startOfMonth
            AND t.departureTime < :endOfMonth
    """)
    BigDecimal countTripByMonth(
            LocalDateTime startOfMonth,
            LocalDateTime endOfMonth
    );
}
