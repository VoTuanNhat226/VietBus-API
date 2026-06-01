package com.vtn.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class RouteRequest {
    private UUID routeId;
    private UUID fromStationId;
    private UUID toStationId;
    private Integer distanceKm;
    private Boolean active;
    private String createdBy;
    private String updatedBy;
}
