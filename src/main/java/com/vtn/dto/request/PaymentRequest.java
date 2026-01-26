package com.vtn.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private String method;
    private String status;

    private String ticketCode;
    private String ticketStatus;
    private String ticketPaymentType;
}
