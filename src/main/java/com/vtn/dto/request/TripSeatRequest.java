package com.vtn.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TripSeatRequest {
    private UUID tripId;
}
