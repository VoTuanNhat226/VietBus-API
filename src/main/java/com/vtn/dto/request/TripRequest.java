package com.vtn.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TripRequest {
    private UUID tripId;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal price;
    private String status;
    private UUID routeId;
    private UUID vehicleId;
    private UUID driverId;
    private UUID fromStationId;
    private UUID toStationId;
}
