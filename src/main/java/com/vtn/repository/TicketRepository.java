package com.vtn.repository;

import com.vtn.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
