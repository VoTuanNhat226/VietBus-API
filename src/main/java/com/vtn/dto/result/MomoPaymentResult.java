package com.vtn.dto.result;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MomoPaymentResult {
    private String payUrl;
    private String qrCodeUrl;
    private String orderId;     // ticketCode
    private String requestId;
}