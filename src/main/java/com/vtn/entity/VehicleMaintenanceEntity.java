package com.vtn.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vtn.enumdef.VehicleMaintenanceStatusEnum;
import com.vtn.enumdef.VehicleMaintenanceTypeEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "vehicle_maintenance_history")
@Entity
public class VehicleMaintenanceEntity extends AuditModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnore
    VehicleEntity vehicle;

    @Enumerated(EnumType.STRING)
    @Column(name = "maintenance_type", nullable = false)
    VehicleMaintenanceTypeEnum maintenanceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    VehicleMaintenanceStatusEnum status;

    @Column(name = "maintenance_date", nullable = false)
    LocalDate maintenanceDate;

    @Column(name = "description", length = 2000)
    String description;

    @Column(name = "odometer_km")
    Integer odometerKm;

    @Column(name = "cost", precision = 15, scale = 2)
    BigDecimal cost;

    @Column(name = "garage_name")
    String garageName;

    @Column(name = "performed_by")
    String performedBy;

    @Column(name = "next_maintenance_date")
    LocalDate nextMaintenanceDate;

    @Column(name = "next_maintenance_km")
    Integer nextMaintenanceKm;

    @Column(name = "invoice_url")
    String invoiceUrl;
}
