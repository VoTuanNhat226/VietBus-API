package com.vtn.entity;

import com.vtn.enumdef.PaymentMethodEnum;
import com.vtn.enumdef.PaymentTypeEnum;
import com.vtn.enumdef.TicketStatusEnum;
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
@Table(name = "tickets")
@Entity
public class TicketEntity extends AuditModel{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID ticketId;

    @Column(name = "ticket_code", nullable = false, unique = true, length = 10)
    String ticketCode;

    @Column(name = "price", nullable = false)
    BigDecimal price;

    @Column(name = "note")
    String note;

    @Column(name = "sold_by")
    String soldBy;

    @Column(name = "sold_at")
    LocalDateTime soldAt;

    @Column(name = "paid_at")
    LocalDateTime paidAt;

    @Column(name = "transaction_id")
    String transactionId;  // ID từ cổng thanh toán

    @Column(name = "momo_pay_url", length = 500)
    String momoPayUrl;

    @Column(name = "momo_qr_code", length = 500)
    String momoQrCode;

    @Column(name = "vnpay_pay_url", length = 1000)
    String vnpayPayUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    TicketStatusEnum status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type")
    PaymentTypeEnum paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    PaymentMethodEnum paymentMethod;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trip_id", nullable = false)
    TripEntity trip;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trip_seat_id", nullable = false)
    TripSeatEntity tripSeat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id")
    PassengerEntity passenger;
}
