package com.vtn.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class TicketRequest {
    private UUID ticketId;
    private BigDecimal ticketPrice;
    private String note;
    private String paymentType;
    private UUID tripId;
    private UUID tripSeatId;
    private UUID passengerId;
    private String paymentMethod;
}
