package com.vtn.service.VNPay;

import com.vtn.dto.result.VNPayPaymentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

@Service
@Slf4j
public class VNPayService {
    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.api-url}")
    private String apiUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Value("${vnpay.ipn-url}")
    private String ipnUrl;

    /**
     * Tạo URL thanh toán VNPay.
     *
     * @param ticketCode mã vé – dùng làm vnp_TxnRef
     * @param price      số tiền (VND, không nhân 100 ở đây – service tự nhân)
     * @param ipAddress  IP của client (bắt buộc theo docs VNPay)
     */
    public VNPayPaymentResult createPayment(String ticketCode, BigDecimal price, String ipAddress) {

        String vnpVersion    = "2.1.0";
        String vnpCommand    = "pay";
        String vnpCurrCode   = "VND";
        String vnpLocale     = "vn";
        String vnpOrderType  = "other";
        String vnpOrderInfo  = "Thanh toan ve " + ticketCode;

        // VNPay yêu cầu amount * 100 (đơn vị: đồng x100)
        long vnpAmount = price.multiply(BigDecimal.valueOf(100)).longValue();

        // Thời gian tạo & hết hạn (15 phút)
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
        fmt.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        Date now     = new Date();
        String createDate = fmt.format(now);
        String expireDate = fmt.format(new Date(now.getTime() + 15 * 60 * 1000));

        // ---- 1. Build param map (TreeMap → tự sort theo key alpha) ----
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version",    vnpVersion);
        params.put("vnp_Command",    vnpCommand);
        params.put("vnp_TmnCode",    tmnCode);
        params.put("vnp_Amount",     String.valueOf(vnpAmount));
        params.put("vnp_CurrCode",   vnpCurrCode);
        String vnpTxnRef = ticketCode + "_" + fmt.format(now);
        params.put("vnp_TxnRef", vnpTxnRef);          // unique order ref
        params.put("vnp_OrderInfo",  vnpOrderInfo);
        params.put("vnp_OrderType",  vnpOrderType);
        params.put("vnp_Locale",     vnpLocale);
        params.put("vnp_ReturnUrl",  returnUrl);
        params.put("vnp_IpAddr",     ipAddress);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        // IPN url (tuỳ chọn – thêm nếu merchant đã đăng ký)
        if (ipnUrl != null && !ipnUrl.isBlank()) {
            params.put("vnp_IpnUrl", ipnUrl);
        }

        // ---- 2. Build query string & tính chữ ký ----
        String hashData  = buildHashData(params);   // key=value&... chưa encode
        String queryStr  = buildQueryString(params); // key=encoded&...
        String signature = hmacSHA512(hashData, hashSecret);

        String payUrl = apiUrl + "?" + queryStr + "&vnp_SecureHash=" + signature;

        log.info("[VNPay] createPayment ticketCode={} amount={} payUrl={}",
                ticketCode, vnpAmount, payUrl);

        return VNPayPaymentResult.builder()
                .payUrl(payUrl)
                .txnRef(ticketCode)
                .build();
    }

    /**
     * Xác minh chữ ký IPN / Return URL từ VNPay gửi về.
     *
     * @param params toàn bộ query params VNPay trả về (dạng Map)
     * @return true nếu chữ ký hợp lệ
     */
    public boolean verifySignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) return false;

        // Loại bỏ các key chữ ký trước khi tính lại
        Map<String, String> filtered = new TreeMap<>(params);
        filtered.remove("vnp_SecureHash");
        filtered.remove("vnp_SecureHashType");

        String hashData   = buildHashData(filtered);
        String myHash     = hmacSHA512(hashData, hashSecret);

        return myHash.equalsIgnoreCase(receivedHash);
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    /** Hash data: KHÔNG encode value */
    private String buildHashData(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> {
            if (v != null && !v.isBlank()) {
                if (!sb.isEmpty()) sb.append('&');
                sb.append(k).append('=').append(v);  // raw, không encode
            }
        });
        return sb.toString();
    }

    /** Query string: CÓ encode value */
    private String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> {
            if (v != null && !v.isBlank()) {
                if (!sb.isEmpty()) sb.append('&');
                sb.append(URLEncoder.encode(k, StandardCharsets.UTF_8).replace("+", "%20"))
                        .append('=')
                        .append(URLEncoder.encode(v, StandardCharsets.UTF_8).replace("+", "%20"));
            }
        });
        return sb.toString();
    }

    /** VNPay dùng HmacSHA512 (khác MoMo dùng SHA256) */
    private String hmacSHA512(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("VNPay hmacSHA512 error", e);
        }
    }
}
