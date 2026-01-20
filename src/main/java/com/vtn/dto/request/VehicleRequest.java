package com.vtn.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class VehicleRequest {
    private UUID vehicleId;
    private String licensePlate;
    private Integer totalSeat;
    private Boolean active;
    private String model;
    private String manufactureYear;
    private Integer totalKm;
}
