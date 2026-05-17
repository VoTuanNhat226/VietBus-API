package com.vtn.dto.response;

import com.vtn.enumdef.PaymentMethodEnum;
import com.vtn.enumdef.PaymentStatusEnum;
import com.vtn.enumdef.PaymentTypeEnum;
import com.vtn.enumdef.TicketStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PaymentResponse {
    private UUID paymentId;
    private PaymentMethodEnum method;
    private PaymentStatusEnum status;
    private BigDecimal amount;
    private LocalDateTime paidAt;
    //Ticket
    private UUID ticketId;
    private String ticketCode;
    private BigDecimal ticketPrice;
    private TicketStatusEnum ticketStatus;
    private PaymentTypeEnum ticketPaymentType;
}
