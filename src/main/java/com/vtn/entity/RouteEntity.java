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
@Table(name = "routes")
@Entity
public class RouteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID routeId;

    @Column(name = "from_station")
    private String fromStation;

    @Column(name = "to_station")
    private String toStation;

    @Column(name = "distance_km")
    private Integer distanceKm;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

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
