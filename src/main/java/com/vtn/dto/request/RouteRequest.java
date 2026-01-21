package com.vtn.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RouteRequest {
    private UUID routeId;
    private UUID fromStationId;
    private UUID toStationId;
    private Integer distanceKm;
    private Integer durationMinutes;
    private Boolean active;
    private String createdBy;
    private String updatedBy;
}
