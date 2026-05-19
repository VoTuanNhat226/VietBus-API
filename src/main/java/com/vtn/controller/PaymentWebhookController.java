package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.momo.MomoCallbackRequest;
import com.vtn.enumdef.PaymentMethodEnum;
import com.vtn.service.Momo.MomoSignatureService;
import com.vtn.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PaymentWebhookController {
    private final PaymentService paymentService;
    private final MomoSignatureService momoSignatureService;

    @Autowired
    public PaymentWebhookController(PaymentService paymentService,
                                    MomoSignatureService momoSignatureService) {
        this.paymentService = paymentService;
        this.momoSignatureService = momoSignatureService;
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
}
