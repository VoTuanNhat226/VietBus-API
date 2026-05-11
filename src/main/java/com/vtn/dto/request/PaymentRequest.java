package com.vtn.dto.request;

import com.vtn.enumdef.PaymentMethodEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private PaymentMethodEnum method;
    private String status;

    private String ticketCode;
    private String ticketStatus;
    private String ticketPaymentType;
}
