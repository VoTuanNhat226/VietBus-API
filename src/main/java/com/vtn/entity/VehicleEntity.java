package com.vtn.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "vehicles")
@Entity
public class VehicleEntity extends AuditModel{
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
}
