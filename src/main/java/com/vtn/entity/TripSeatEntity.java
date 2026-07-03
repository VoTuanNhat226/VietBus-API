package com.vtn.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vtn.enumdef.TripSeatStatusEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "trip_seats", uniqueConstraints = @UniqueConstraint(columnNames = {"trip_id", "seat_id"}))
@Entity
public class TripSeatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    TripSeatStatusEnum status;

    @Column(name = "processing_staff")
    String processingStaff;

    @Column(name = "processing_at")
    LocalDateTime processingAt;

    @Column(name = "processing_expired_at")
    LocalDateTime processingExpiredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    @JsonIgnore
    TripEntity trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    SeatEntity seat;
}
