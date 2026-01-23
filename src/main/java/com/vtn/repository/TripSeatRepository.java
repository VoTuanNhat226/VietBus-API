package com.vtn.repository;

import com.vtn.entity.TripSeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TripSeatRepository extends JpaRepository<TripSeatEntity, UUID> {
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
