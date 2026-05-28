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
@Table(name = "seats", uniqueConstraints = {@UniqueConstraint(name = "uk_seat_number_vehicle", columnNames = {"seat_number", "vehicle_id"})})
@Entity
public class SeatEntity extends AuditModel{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID seatId;

    @Column(name = "seat_number", nullable = false)
    String seatNumber;

    @Column(name = "floor", nullable = false)
    Integer floor; // 1 or 2

    @Column(name = "seat_row", nullable = false)
    Integer seatRow; // row (1 → 6)

    @Column(name = "seat_column", nullable = false)
    String seatColumn; // A, B, C

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false, foreignKey = @ForeignKey(name = "fk_seat_vehicle_id"))
    @JsonIgnore
    VehicleEntity vehicle;
}
