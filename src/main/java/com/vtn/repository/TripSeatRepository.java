package com.vtn.repository;

import com.vtn.entity.TripSeatEntity;
import com.vtn.enumdef.TripSeatStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        JOIN FETCH t.trip
        JOIN FETCH t.seat
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

    @Modifying
    @Transactional
    @Query("""
        UPDATE TripSeatEntity ts
        SET ts.processingStaff = null,
            ts.processingAt = null,
            ts.processingExpiredAt = null,
            ts.status = 'AVAILABLE'
        WHERE ts.trip.tripId = :tripId
        AND ts.status = :status
        AND ts.processingExpiredAt <= :now
    """)
    void clearExpiredProcessing(UUID tripId, TripSeatStatusEnum status, LocalDateTime now);

    @Modifying
    @Query("""
        UPDATE TripSeatEntity t
        SET t.status = 'BLOCKED',
            t.processingStaff = :username,
            t.processingAt = :now,
            t.processingExpiredAt = :expiredAt
        WHERE t.id = :tripSeatId
          AND t.status NOT IN ('SOLD', 'HOLD')
          AND (t.processingStaff IS NULL
              OR t.processingStaff = :username
              OR t.processingExpiredAt <= :now
          )
    """)
    int tryLockSeat(
            @Param("tripSeatId") UUID tripSeatId,
            @Param("username") String username,
            @Param("now") LocalDateTime now,
            @Param("expiredAt") LocalDateTime expiredAt
    );

    @Modifying
    @Query("""
        UPDATE TripSeatEntity t
        SET t.status = 'AVAILABLE',
            t.processingStaff = NULL,
            t.processingAt = NULL,
            t.processingExpiredAt = NULL
        WHERE t.id = :tripSeatId
            AND t.status NOT IN ('HOLD','SOLD')
            AND t.processingStaff IS NOT NULL
            AND (t.processingStaff = :username OR t.processingExpiredAt <= :now)
    """)
    int tryUnlockSeat(
            @Param("tripSeatId") UUID tripSeatId,
            @Param("username") String username,
            @Param("now") LocalDateTime now
    );
};
