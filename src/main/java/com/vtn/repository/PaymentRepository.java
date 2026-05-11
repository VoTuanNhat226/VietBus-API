package com.vtn.repository;

import com.vtn.entity.PaymentEntity;
import com.vtn.enumdef.PaymentMethodEnum;
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
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    @Query("""
        SELECT p
        FROM PaymentEntity p
        WHERE (:method IS NULL OR p.method = :method)
            AND (:status IS NULL OR p.status = :status)
            AND (:ticketCode IS NULL OR p.ticket.ticketCode LIKE %:ticketCode%)
            AND (:ticketStatus IS NULL OR p.ticket.status = :ticketStatus)
            AND (:ticketPaymentType IS NULL OR p.ticket.paymentType = :ticketPaymentType)
    """)
    List<PaymentEntity> getAllByCondition(
            @Param("method") PaymentMethodEnum method,
            @Param("status") String status,
            @Param("ticketCode") String ticketCode,
            @Param("ticketStatus") String ticketStatus,
            @Param("ticketPaymentType") String ticketPaymentType
    );

    @Query("""
        SELECT SUM(p.amount)
        FROM PaymentEntity p
        WHERE p.status = :status
            AND p.paidAt >= :startOfMonth
            AND p.paidAt < :endOfMonth
    """)
    BigDecimal getRevenueByMonth(
            PaymentStatusEnum status,
            LocalDateTime startOfMonth,
            LocalDateTime endOfMonth
    );
}
