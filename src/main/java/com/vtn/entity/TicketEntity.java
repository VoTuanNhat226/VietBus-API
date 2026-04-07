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
    private UUID ticketId;

    @Column(name = "ticket_code", nullable = false, unique = true, length = 10)
    private String ticketCode;

    @Column(name = "note")
    private String note;

    @Column(name = "price")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatusEnum status;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "sold_by")
    private String soldBy;

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_seat_id", nullable = false, unique = true)
    private TripSeatEntity tripSeat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id")
    private PassengerEntity passenger;
}
