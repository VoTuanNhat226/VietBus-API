package com.vtn.service.Momo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vtn.dto.result.MomoPaymentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class MomoService {
    @Value("${momo.partner-code}")   private String partnerCode;
    @Value("${momo.access-key}")     private String accessKey;
    @Value("${momo.secret-key}")     private String secretKey;
    @Value("${momo.api-url}")        private String apiUrl;
    @Value("${momo.redirect-url}")   private String redirectUrl;
    @Value("${momo.ipn-url}")        private String ipnUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MomoPaymentResult createPayment(String ticketCode, BigDecimal price)
            throws Exception {

        String orderId   = ticketCode;           // dùng ticketCode làm orderId -> dễ tra sau
        String requestId = UUID.randomUUID().toString();
        long   amount    = price.longValue();
        String extraData = "";                   // để trống or base64
        String requestType = "captureWallet";
        String orderInfo  = "Pay ticket " + ticketCode;

        // 1. Raw signature - thứ tự CỐ ĐỊNH theo docs MoMo
        String rawSignature =
                "accessKey="   + accessKey   +
                        "&amount="     + amount      +
                        "&extraData="  + extraData   +
                        "&ipnUrl="     + ipnUrl      +
                        "&orderId="    + orderId     +
                        "&orderInfo="  + orderInfo   +
                        "&partnerCode="+ partnerCode +
                        "&redirectUrl="+ redirectUrl +
                        "&requestId="  + requestId   +
                        "&requestType="+ requestType;

        String signature = hmacSHA256(rawSignature, secretKey);

        // 2. Request body
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("accessKey",   accessKey);
        body.put("requestId",   requestId);
        body.put("amount",      amount);
        body.put("orderId",     orderId);
        body.put("orderInfo",   orderInfo);
        body.put("redirectUrl", redirectUrl);
        body.put("ipnUrl",      ipnUrl);
        body.put("extraData",   extraData);
        body.put("requestType", requestType);
        body.put("signature",   signature);
        body.put("lang",        "vi");

        // 3. Call MoMo
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
        Map<?, ?> result = objectMapper.readValue(response.body(), Map.class);

        int resultCode = (int) result.get("resultCode");
        if (resultCode != 0) {
            throw new RuntimeException("MoMo error: " + result.get("message"));
        }

        return MomoPaymentResult.builder()
                .payUrl((String) result.get("payUrl"))
                .qrCodeUrl((String) result.get("qrCodeUrl"))
                .orderId(orderId)
                .requestId(requestId)
                .build();
    }

    private String hmacSHA256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
