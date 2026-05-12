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
public class StationEntity extends AuditModel{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID stationId;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "city", nullable = false)
    String city;

    @Column(name = "address")
    String address;
}
