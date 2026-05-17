package com.vtn.dto.request;

import com.vtn.enumdef.PaymentMethodEnum;
import com.vtn.enumdef.PaymentTypeEnum;
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
    private PaymentTypeEnum paymentType;
    private PaymentMethodEnum paymentMethod;

    private UUID tripId;
    private UUID tripSeatId;
    private String tripCode;

    private UUID passengerId;
    private String passengerName;
    private String passengerPhoneNumber;
}
