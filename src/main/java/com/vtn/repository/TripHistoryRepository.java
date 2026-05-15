package com.vtn.repository;

import com.vtn.entity.TripHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripHistoryRepository extends JpaRepository<TripHistory, Long> {
    @Query("""
        SELECT th
        FROM TripHistory th
        JOIN FETCH th.trip
        WHERE th.trip.tripId = :tripId
    """)
    List<TripHistory> findByTripId(@Param("tripId") UUID tripId);
}
