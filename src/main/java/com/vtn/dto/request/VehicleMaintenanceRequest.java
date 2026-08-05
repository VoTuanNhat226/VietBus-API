package com.vtn.dto.request;

import com.vtn.enumdef.VehicleMaintenanceStatusEnum;
import com.vtn.enumdef.VehicleMaintenanceTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class VehicleMaintenanceRequest {
    private UUID id;
    private UUID vehicleId;
    private VehicleMaintenanceTypeEnum maintenanceType;
    private VehicleMaintenanceStatusEnum status;
    private LocalDate maintenanceDate;
    private String description;
    private Integer odometerKm;
    private BigDecimal cost;
    private String garageName;
    private String performedBy;
    private LocalDate nextMaintenanceDate;
    private Integer nextMaintenanceKm;
    private String invoiceUrl;
}
