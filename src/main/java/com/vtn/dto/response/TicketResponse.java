package com.vtn.dto.response;

import com.vtn.enumdef.PaymentTypeEnum;
import com.vtn.enumdef.TicketStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TicketResponse {
    private UUID ticketId;
    private String ticketCode;
    private BigDecimal ticketPrice;
    private TicketStatusEnum ticketStatus;
    private PaymentTypeEnum ticketPaymentType;
    private String ticketSoldBy;
    private LocalDateTime ticketSoldAt;
    private String ticketNote;

    private UUID tripId;
    private String tripCode;
    private String fromStation;
    private String toStation;

    private String seatNumber;

    private String passengerName;
    private String passengerPhone;
    private String passengerNote;
}
