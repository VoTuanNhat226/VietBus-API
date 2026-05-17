package com.vtn.repository;

import com.vtn.dto.response.TicketRouteStats;
import com.vtn.entity.TicketEntity;
import com.vtn.enumdef.PaymentStatusEnum;
import com.vtn.enumdef.TicketStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, Integer> {
    boolean existsByTicketCode(String ticketCode);

    @Query("""
        SELECT t
        FROM TicketEntity t
        WHERE t.ticketCode = :ticketCode
    """)
    TicketEntity findByTicketCode(@Param("ticketCode") String ticketCode);

    @Query("""
        SELECT t
        FROM TicketEntity t
        LEFT JOIN FETCH t.passenger p
        LEFT JOIN t.trip tr
        WHERE (:ticketCode IS NULL OR t.ticketCode = :ticketCode)
            AND (:ticketStatus IS NULL OR t.status = :ticketStatus)
            AND (:tripCode IS NULL OR tr.tripCode = :tripCode)
            AND (:tripId IS NULL OR tr.tripId = :tripId)
            AND (:ticketPaymentType IS NULL OR t.paymentType = :ticketPaymentType)
            AND (:passengerName IS NULL OR p.fullName LIKE %:passengerName%)
            AND (:passengerPhoneNumber IS NULL OR p.phoneNumber = :passengerPhoneNumber)
    """)
    List<TicketEntity> getAllTicketByCondition(
            @Param("ticketCode") String ticketCode,
            @Param("ticketStatus") TicketStatusEnum ticketStatus,
            @Param("tripCode") String tripCode,
            @Param("tripId") UUID tripId,
            @Param("ticketPaymentType") String ticketPaymentType,
            @Param("passengerName") String passengerName,
            @Param("passengerPhoneNumber") String passengerPhoneNumber
    );

    @Query("""
        SELECT t
        FROM TicketEntity t
        WHERE (t.status = 'UNPAID')
            AND (:ticketCode IS NULL OR t.ticketCode LIKE %:ticketCode%)
            AND (:tripCode IS NULL OR t.trip.tripCode LIKE %:tripCode%)
            AND (:ticketPaymentType IS NULL OR t.paymentType = :ticketPaymentType)
            AND (:ticketSoldBy IS NULL OR t.soldBy LIKE %:ticketSoldBy%)
    """)
    List<TicketEntity> getAllTicketUnpaid(
            @Param("ticketCode") String ticketCode,
            @Param("tripCode") String tripCode,
            @Param("ticketPaymentType") String ticketPaymentType,
            @Param("ticketSoldBy") String ticketSoldBy
    );

    @Query("""
        SELECT t
        FROM TicketEntity t
        LEFT JOIN FETCH t.passenger p
        LEFT JOIN t.trip tr
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
