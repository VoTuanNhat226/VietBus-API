package com.vtn.repository;

import com.vtn.dto.response.TicketRouteStats;
import com.vtn.entity.TicketEntity;
import com.vtn.enumdef.PaymentStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, Integer> {
    boolean existsByTicketCode(String ticketCode);

    TicketEntity findByTicketCode(String ticketCode);

    @Query("""
        SELECT t
        FROM TicketEntity t
        WHERE t.status = 'UNPAID'
    """)
    List<TicketEntity> findAllTicketUnPaid();

    @Query("""
        SELECT t
        FROM TicketEntity t
        WHERE (t.status = 'UNPAID')
            AND (:ticketCode IS NULL OR t.ticketCode LIKE %:ticketCode%)
            AND (:tripCode IS NULL OR t.trip.tripCode LIKE %:tripCode%)
            AND (:ticketPaymentType IS NULL OR t.paymentType = :ticketPaymentType)
            AND (:ticketSoldBy IS NULL OR t.soldBy LIKE %:ticketSoldBy%)
    """)
    List<TicketEntity> getAllByCondition(
            @Param("ticketCode") String ticketCode,
            @Param("tripCode") String tripCode,
            @Param("ticketPaymentType") String ticketPaymentType,
            @Param("ticketSoldBy") String ticketSoldBy
    );

    @Query("""
        SELECT t
        FROM TicketEntity t
        JOIN TripEntity tr ON t.trip.tripId = tr.tripId
        WHERE tr.tripId = :tripId
    """)
    List<TicketEntity> getAllByTripId(
            @Param("tripId") UUID tripId
    );

    @Query("""
        SELECT COUNT(t)
        FROM TicketEntity t
        WHERE t.createdAt >= :startOfMonth
            AND t.createdAt < :endOfMonth
    """)
    BigDecimal countTicketByMonth(
            LocalDateTime startOfMonth,
            LocalDateTime endOfMonth
    );

    @Query("""
        SELECT new com.vtn.dto.response.TicketRouteStats(
            count(t),
            r.fromStation.name,
            r.toStation.name
        )
        FROM TicketEntity t
        JOIN t.trip tr
        JOIN tr.route r
        WHERE tr.departureTime >= :startOfMonth
            AND tr.arrivalTime <= :endOfMonth
        GROUP BY r.fromStation.name, r.toStation.name
    """)
    List<TicketRouteStats> countTicketPerRoute(
            LocalDateTime startOfMonth,
            LocalDateTime endOfMonth
    );
}
