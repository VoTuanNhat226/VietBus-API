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
@Table(name = "stations")
@Entity
public class StationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID stationId;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "city", nullable = false)
    String city;

    @Column(name = "address")
    String address;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "created_by")
    String createdBy;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Column(name = "updated_by")
    String updatedBy;
}
