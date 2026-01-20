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
    private UUID vehicleId;

    @Column(name = "license_plate")
    private String licensePlate;

    @Column(name = "total_seat")
    private Integer totalSeat;

    @Column(name = "active")
    private boolean active;

    @Column(name = "model")
    private String model;

    @Column(name = "manufacture_year")
    private String manufactureYear;

    @Column(name = "total_km")
    private Integer totalKm;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;
}
