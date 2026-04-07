package com.vtn.dto.request;

import com.vtn.enumdef.TicketStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class TicketRequest {
    private UUID ticketId;
    private String ticketCode;
    private BigDecimal ticketPrice;
    private TicketStatusEnum ticketStatus;
    private String ticketPaymentType;
    private String ticketSoldBy;

    private String note;
    private String paymentType;

    private UUID tripId;
    private UUID tripSeatId;
    private String tripCode;

    private UUID passengerId;
    private String paymentMethod;
}
