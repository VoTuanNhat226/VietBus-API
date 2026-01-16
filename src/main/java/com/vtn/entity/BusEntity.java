package com.vtn.entity;

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
@Table(name = "buses")
@Entity
public class BusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID busId;

    @Column(name = "license_plate")
    private String licensePlate;

    @Column(name = "total_seat")
    private Integer totalSeat;

    @Column(name = "active")
    private boolean active;

    @Column(name = "created_at")
    private LocalDateTime created_at;

    @Column(name = "created_by")
    private String created_by;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "updated_by")
    private String updated_by;
}
