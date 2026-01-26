package com.vtn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PaymentResponse {
    private UUID paymentId;
    private String method;
    private String status;
    private BigDecimal amount;
    private LocalDateTime paidAt;
    //Ticket
    private UUID ticketId;
    private String ticketCode;
    private BigDecimal ticketPrice;
    private String ticketStatus;
    private String ticketPaymentType;
}
