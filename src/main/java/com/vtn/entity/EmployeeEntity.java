package com.vtn.entity;

import com.vtn.enumdef.EmployeePositionEnum;
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
@Table(name = "employees")
@Entity
public class EmployeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID employeeId;

    @Column(name = "full_name", nullable = false)
    String fullName;

    @Column(name = "phone_number", nullable = false)
    String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false)
    EmployeePositionEnum position;

    @Column(name = "active")
    boolean active;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "created_by")
    String createdBy;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Column(name = "updated_by")
    String updatedBy;

    @OneToOne
    @JoinColumn(name = "account_id", unique = true, foreignKey = @ForeignKey(name = "fk_employee_account_id"))
    AccountEntity account;
}
