package com.vtn.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "payments")
@Entity
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;

    @Column(name = "method")
    private String method; // CASH, VNPAY, MOMO

    @Column(name = "status")
    private String status; // PENDING, SUCCESS, FAILED, REFUNDED

    @Column(name = "amount",nullable = false)
    private BigDecimal amount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at")
    private LocalDateTime created_at;

    @Column(name = "created_by")
    private String created_by;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "updated_by")
    private String updated_by;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketEntity ticket;
}
