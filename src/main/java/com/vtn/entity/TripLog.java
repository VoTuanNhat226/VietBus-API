package com.vtn.entity;

import com.vtn.enumdef.TripStatusEnum;
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
@Table(name = "trip_log")
@Entity
public class TripLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "staff", nullable = false)
    String staff;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    TripStatusEnum status;

    @Column(name = "change_at", nullable = false)
    LocalDateTime changeAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    TripEntity trip;
}
