package com.vtn.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class BusRequest {
    private UUID busId;
    private String licensePlate;
    private Integer totalSeat;
    private boolean active;
}
