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
@Table(name = "passengers")
@Entity
public class PassengerEntity extends AuditModel{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID passengerId;

    @Column(name = "full_name", nullable = false)
    String fullName;

    @Column(name = "phone_number", nullable = false, unique = true)
    String phoneNumber;

    @Column(name = "email", nullable = false, unique = true)
    String email;

    @Column(name = "id_card", nullable = false, unique = true)
    String idCard;

    @Column(name = "note")
    String note;
}
