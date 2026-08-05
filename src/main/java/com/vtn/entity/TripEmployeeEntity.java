package com.vtn.entity;

import com.vtn.enumdef.AccountRoleEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "trip_employees")
@Entity
public class TripEmployeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID tripEmployeeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trip_id", nullable = false, foreignKey = @ForeignKey(name = "fk_trip_employee_trip_id"))
    TripEntity trip;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_trip_employee_employee_id"))
    EmployeeEntity employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    AccountRoleEnum role;
}
