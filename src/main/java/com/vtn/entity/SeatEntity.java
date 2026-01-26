package com.vtn.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "seats", uniqueConstraints = {@UniqueConstraint(name = "uk_seat_number_bus", columnNames = {"seat_number", "bus_id"})})
@Entity
public class SeatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID seatId;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(name = "floor")
    private Integer floor; // 1 hoặc 2

    @Column(name = "seat_row")
    private Integer seatRow; // hàng (1 → 6)

    @Column(name = "seat_column")
    private String seatColumn; // A, B, C

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false, foreignKey = @ForeignKey(name = "fk_seat_vehicle_id"))
    @JsonIgnore
    VehicleEntity vehicle;
}
