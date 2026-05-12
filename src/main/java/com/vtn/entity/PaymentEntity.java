package com.vtn.entity;

import com.vtn.enumdef.PaymentMethodEnum;
import com.vtn.enumdef.PaymentStatusEnum;
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
public class PaymentEntity extends AuditModel{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method")
    PaymentMethodEnum method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    PaymentStatusEnum status;

    @Column(name = "amount",nullable = false)
    BigDecimal amount;

    @Column(name = "paid_at")
    LocalDateTime paidAt;

    @OneToOne
    @JoinColumn(name = "ticket_id", unique = true)
    TicketEntity ticket;
}
