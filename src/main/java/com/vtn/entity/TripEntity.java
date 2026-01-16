package com.vtn.entity;

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
@Table(name = "trips")
@Entity
public class TripEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tripId;

    @Column(name = "departure_time", nullable = false)
    LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    LocalDateTime arrivalTime;

    @Column(name = "price", nullable = false)
    BigDecimal price;

    @Column(name = "status")
    String status; //OPEN - RUNNING - CLOSED

    @Column(name = "created_at")
    private LocalDateTime created_at;

    @Column(name = "created_by")
    private String created_by;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "updated_by")
    private String updated_by;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false, foreignKey = @ForeignKey(name = "fk_trip_route_id"))
    RouteEntity route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false, foreignKey = @ForeignKey(name = "fk_trip_bus_id"))
    BusEntity bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_trip_employee_id"))
    EmployeeEntity driver;
}
