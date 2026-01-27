package com.vtn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TicketResponse {
    private UUID ticketId;
    private String ticketCode;
    private BigDecimal ticketPrice;
    private String ticketStatus; //UNPAID - PAID
    private String ticketPaymentType; //PAY_NOW - PAY_LATER
    private String ticketSoldBy;

    private String tripCode;
    private String fromStation;
    private String toStation;

    private String seatNumber;
}
