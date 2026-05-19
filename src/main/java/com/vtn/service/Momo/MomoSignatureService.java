package com.vtn.service.Momo;

import com.vtn.dto.request.momo.MomoCallbackRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class MomoSignatureService {
    @Value("${momo.secret-key}")
    private String secretKey;
    @Value("${momo.access-key}")
    private String momoAccessKey;

    public boolean verify(MomoCallbackRequest req) {
        // Thứ tự này là cố định theo docs MOMO v2
        String rawData = "accessKey="    + momoAccessKey +
                "&amount="      + req.getAmount() +
                "&extraData="   + req.getExtraData() +
                "&message="     + req.getMessage() +
                "&orderId="     + req.getOrderId() +
                "&orderInfo="   + req.getOrderInfo() +
                "&orderType="   + req.getOrderType() +
                "&partnerCode=" + req.getPartnerCode() +
                "&payType="     + req.getPayType() +
                "&requestId="   + req.getRequestId() +
                "&responseTime="+ req.getResponseTime() +
                "&resultCode="  + req.getResultCode() +
                "&transId="     + req.getTransId();

        String expected = hmacSHA256(rawData, secretKey);
        return expected.equals(req.getSignature());
    }

    private String hmacSHA256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 error", e);
        }
    }
}
