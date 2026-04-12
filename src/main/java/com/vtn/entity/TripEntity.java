package com.vtn.entity;

import com.vtn.enumdef.AccountRoleEnum;
import com.vtn.enumdef.TripStatusEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "trips")
@Entity
public class TripEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID tripId;

    @Column(name = "trip_code", nullable = false, unique = true, length = 10)
    String tripCode;

    @Column(name = "departure_time", nullable = false)
    LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    LocalDateTime arrivalTime;

    @Column(name = "price", nullable = false)
    BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    TripStatusEnum status;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "created_by")
    String createdBy;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Column(name = "updated_by")
    String updatedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "route_id", nullable = false, foreignKey = @ForeignKey(name = "fk_trip_route_id"))
    RouteEntity route;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id", nullable = false, foreignKey = @ForeignKey(name = "fk_trip_vehicle_id"))
    VehicleEntity vehicle;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    List<TripEmployeeEntity> tripEmployees = new ArrayList<>();

    // ── Helpers ──────────────────────────────────────────────────────────────
    /** Trả về danh sách tất cả tài xế của chuyến */
    public List<EmployeeEntity> getDrivers() {
        return tripEmployees.stream()
                .filter(te -> te.getRole() == AccountRoleEnum.DRIVER)
                .map(TripEmployeeEntity::getEmployee)
                .collect(Collectors.toList());
    }

    /** Trả về danh sách tất cả phụ xe của chuyến */
    public List<EmployeeEntity> getAssistants() {
        return tripEmployees.stream()
                .filter(te -> te.getRole() == AccountRoleEnum.ASSISTANT)
                .map(TripEmployeeEntity::getEmployee)
                .collect(Collectors.toList());
    }

    /** Thêm một nhân viên vào chuyến với vai trò cho trước */
    public void addEmployee(EmployeeEntity employee, AccountRoleEnum role) {
        TripEmployeeEntity te = new TripEmployeeEntity();
        te.setTrip(this);
        te.setEmployee(employee);
        te.setRole(role);
        tripEmployees.add(te);
    }

    /** Xóa một nhân viên khỏi chuyến */
    public void removeEmployee(EmployeeEntity employee) {
        tripEmployees.removeIf(te -> te.getEmployee().equals(employee));
    }
}
