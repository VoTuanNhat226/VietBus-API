package com.vtn.dto.result;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VNPayPaymentResult {
    private String payUrl;
    private String txnRef;   // = ticketCode
}
