package com.vtn.dto.request.momo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MomoCallbackRequest {
    // Định danh đối tác - do MOMO cấp khi đăng ký
    private String partnerCode;

    // ID request lúc bạn tạo thanh toán (bạn tự sinh, UUID)
    private String requestId;

    // Số tiền giao dịch (VND, không có phần thập phân)
    private Long amount;

    // Mã đơn hàng - chính là ticketCode bạn truyền vào lúc tạo
    private String orderId;

    // Mô tả đơn hàng bạn đã gửi lúc tạo
    private String orderInfo;

    // Loại đơn hàng, thường là "momo_wallet"
    private String orderType;

    // ID giao dịch phía MOMO (dùng để lưu vào DB, đối soát sau)
    private Long transId;

    // Mã kết quả: 0 = thành công, khác 0 = thất bại/hủy
    private Integer resultCode;

    // Mô tả kết quả bằng tiếng Anh
    private String message;

    // Thời điểm MOMO xử lý xong (epoch milliseconds)
    private Long responseTime;

    // Loại thanh toán: "qr", "webApp", "miniapp", ...
    private String payType;

    // Dữ liệu extra bạn truyền vào lúc tạo (base64, có thể rỗng)
    private String extraData;

    // Chữ ký HMAC-SHA256 để verify tính xác thực của request
    private String signature;
}
