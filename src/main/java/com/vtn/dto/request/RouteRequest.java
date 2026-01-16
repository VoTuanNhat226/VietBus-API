package com.vtn.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RouteRequest {
    private UUID routeId;
    private String fromStation;
    private String toStation;
    private Integer distanceKm;
    private Integer durationMinutes;
    private boolean active;
}
