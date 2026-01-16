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
@Table(name = "seats")
@Entity
public class SeatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID seatId;

    @Column(name = "seat_number")
    private String seatNumber;

    @Column(name = "bus_license_plate")
    private String busLicensePlate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false, foreignKey = @ForeignKey(name = "fk_seat_bus_id"))
    BusEntity bus;
}
