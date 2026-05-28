package com.vtn.repository;

import com.vtn.entity.TripSeatEntity;
import com.vtn.enumdef.TripSeatStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripSeatRepository extends JpaRepository<TripSeatEntity, UUID> {
    @Query("""
        SELECT ts
        FROM TripSeatEntity ts
        WHERE ts.id = :tripSeatId
    """)
    TripSeatEntity findByTripSeatId(@Param("tripSeatId") UUID tripSeatId);

    @Query("""
        SELECT ts
        FROM TripSeatEntity ts
        WHERE ts.id IN :tripSeatIds
    """)
    List<TripSeatEntity> findAllById(@Param("tripSeatIds") List<UUID> tripSeatIds);

    @Query("""
        SELECT COUNT(t)
        FROM TripSeatEntity t
        WHERE t.trip.tripId = :tripId
            AND t.status IN :statuses
    """)
    Integer countTripSeatSoldByTripId(
            @Param("tripId") UUID tripId,
            @Param("statuses") List<TripSeatStatusEnum> statuses);

    @Query("""
        SELECT t
        FROM TripSeatEntity t
        WHERE t.trip.tripId = :tripId
    """)
    List<TripSeatEntity> findAllTripSeatsByTripId(UUID tripId);

    @Query("""
        SELECT ts
        FROM TripSeatEntity ts
        JOIN FETCH TripEntity t ON t.tripId = ts.trip.tripId
        WHERE t.tripId = :tripId
            AND t.status = 'OPEN_FOR_BOOKING'
            AND ts.status = 'AVAILABLE'
    """)
    List<TripSeatEntity> findAllTripSeatAvailableByTripId(@Param("tripId") UUID tripId);
}
