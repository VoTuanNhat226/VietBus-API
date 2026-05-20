package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.momo.MomoCallbackRequest;
import com.vtn.enumdef.PaymentMethodEnum;
import com.vtn.service.Momo.MomoSignatureService;
import com.vtn.service.PaymentService;
import com.vtn.service.VNPay.VNPaySignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PaymentWebhookController {
    private final PaymentService paymentService;
    private final MomoSignatureService momoSignatureService;
    private final VNPaySignatureService vnPaySignatureService;

    @Autowired
    public PaymentWebhookController(PaymentService paymentService,
                                    MomoSignatureService momoSignatureService,
                                    VNPaySignatureService vnPaySignatureService) {
        this.paymentService = paymentService;
        this.momoSignatureService = momoSignatureService;
        this.vnPaySignatureService = vnPaySignatureService;
    }

    @PostMapping(value = APIConstants.API_WEBHOOK_MOMO)
    public ResponseEntity<Map<String, Object>> momoCallback(@RequestBody MomoCallbackRequest request) throws Exception {
        // Step 1: Verify signature HMAC
        boolean valid = momoSignatureService.verify(request);
        if (!valid) {
            return ResponseEntity.badRequest().body(Map.of("resultCode", 1, "message", "Invalid signature"));
        }

        // Step 2: Check resultCode (0 = success)
        if (request.getResultCode() == 0) {
            // Step 3: Update DB
            paymentService.confirmPayment(
                    request.getOrderId(),                   // ticketCode truyền vào lúc tạo
                    String.valueOf(request.getTransId()),   // transaction ID từ MOMO
                    PaymentMethodEnum.MOMO
            );
        }

        // Step 4: Return 200 OK for MOMO not callback
        return ResponseEntity.ok(Map.of("resultCode", 0, "message", "Success"));
    }

    @PostMapping(value = APIConstants.API_WEBHOOK_VNPAY)
    public ResponseEntity<Map<String, Object>> vnpayCallback(@RequestParam Map<String, String> params) throws Exception {
        // Step 1: Verify signature
        boolean valid = vnPaySignatureService.verify(params);
        if (!valid) {
            return ResponseEntity.ok(Map.of("RspCode", "97", "Message", "Invalid signature"));
        }

        // Step 2: Check vnp_ResponseCode ("00" = success)
        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            // Step 3: Update DB
            paymentService.confirmPayment(
                    params.get("vnp_TxnRef"),          // ticketCode truyền vào lúc tạo
                    params.get("vnp_TransactionNo"),   // transaction ID từ VNPay
                    PaymentMethodEnum.VNPAY
            );
        }

        // Step 4: Return 200 OK for VNPay not callback
        return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
    }
}
