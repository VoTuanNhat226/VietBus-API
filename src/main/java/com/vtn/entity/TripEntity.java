package com.vtn.entity;

import com.vtn.enumdef.TripStatusEnum;
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

    @Column(name = "trip_code", nullable = false, unique = true, length = 10)
    private String tripCode;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "rest_stop")
    private String restStop;

    @Column(name = "rest_time")
    private LocalDateTime restTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TripStatusEnum status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "route_id", nullable = false, foreignKey = @ForeignKey(name = "fk_trip_route_id"))
    RouteEntity route;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id", nullable = false, foreignKey = @ForeignKey(name = "fk_trip_vehicle_id"))
    VehicleEntity vehicle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id", nullable = false, foreignKey = @ForeignKey(name = "fk_trip_driver_id"))
    EmployeeEntity driver;
}
