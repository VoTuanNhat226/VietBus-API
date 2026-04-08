package com.vtn.entity;

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
public class TicketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID ticketId;

    @Column(name = "ticket_code", nullable = false, unique = true, length = 10)
    String ticketCode;

    @Column(name = "note")
    String note;

    @Column(name = "price", nullable = false)
    BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    TicketStatusEnum status;

    @Column(name = "payment_type")
    String paymentType;

    @Column(name = "sold_by")
    String soldBy;

    @Column(name = "sold_at")
    LocalDateTime soldAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    String createdBy;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Column(name = "updated_by")
    String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    TripEntity trip;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_seat_id", nullable = false, unique = true)
    TripSeatEntity tripSeat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id")
    PassengerEntity passenger;
}
