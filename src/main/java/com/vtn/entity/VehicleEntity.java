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
@Table(name = "vehicles")
@Entity
public class VehicleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID vehicleId;

    @Column(name = "license_plate", nullable = false)
    String licensePlate;

    @Column(name = "total_seat", nullable = false)
    Integer totalSeat;

    @Column(name = "active")
    boolean active;

    @Column(name = "model")
    String model;

    @Column(name = "manufacture_year")
    String manufactureYear;

    @Column(name = "total_km")
    Integer totalKm;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "created_by")
    String createdBy;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Column(name = "updated_by")
    String updatedBy;
}
