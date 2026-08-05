package com.vtn.service.Mail;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vtn.dto.result.MomoPaymentResult;
import com.vtn.dto.result.VNPayPaymentResult;
import com.vtn.entity.TicketEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendTicketMail(String to, String subject, String htmlContent, byte[] qrCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            if (qrCode != null) {
                helper.addInline("ticketQr", new ByteArrayResource(qrCode), "image/png");
            }

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Send mail failed", e);
        }
    }

    public String buildTicketMailContent(TicketEntity ticket) {
        String bookingTime = ticket.getCreatedAt().format(MAIL_DATE_FORMAT);
        String departureTime = ticket.getTrip().getDepartureTime().format(MAIL_DATE_FORMAT);
        String arrivalTime = ticket.getTrip().getArrivalTime().format(MAIL_DATE_FORMAT);

        return """
        <div style='font-family:Arial,sans-serif; max-width:600px; margin:auto;'>
            <p>Xin chào, cảm ơn bạn đã đặt vé tại <b>VietBus</b>.</p>
        
            <div style='margin:20px 0; padding:16px; border:1px solid #eee; border-radius:8px;'>
                <table style='width:100%%; border-collapse:collapse;'>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Mã vé</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Ngày đặt</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Điểm đi</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Giờ xuất bến</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Điểm đến</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Giờ đến</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Số ghế</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Giá vé</b></td>
                        <td style='padding:8px; color:#1976d2; font-weight:bold;'>%,d VND</td>
                    </tr>
                </table>
            </div>
        
            <div style='margin:24px 0; text-align:center;'>
                <h3 style='margin-bottom:12px;'>QR Check-in</h3>
                <img src="cid:ticketQr"
                     width="250"
                     style='border:1px solid #ddd; padding:8px; border-radius:8px; background:white;'/>
            </div>
        
            <div style='padding:16px; background:#f8f9fa; border-radius:8px; color:#555;'>
                <p style='margin:0 0 8px;'>
                    Vui lòng đưa mã QR cho nhân viên khi lên xe.
                </p>
                <p style='margin:0;'>
                    Có mặt tại bến xe trước giờ khởi hành ít nhất <b>15 phút</b>.
                </p>
            </div>
        
            <p style='margin-top:24px;'>
                Nếu cần hỗ trợ, vui lòng liên hệ hotline VietBus: 0977751951.
            </p>
            <p style='margin-top:24px;'>
                Cảm ơn bạn đã sử dụng dịch vụ của VietBus!
            </p>
        </div>
        """.formatted(
                ticket.getTicketCode(),
                bookingTime,
                ticket.getTrip().getRoute().getFromStation().getName(),
                departureTime,
                ticket.getTrip().getRoute().getToStation().getName(),
                arrivalTime,
                ticket.getTripSeat().getSeat().getSeatNumber(),
                ticket.getPrice().longValue()
        );
    }

    public void sendHtmlMail(String to, String subject, String htmlContent, byte[] imageBytes, String cid) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            if (imageBytes != null && cid != null) {
                helper.addInline(cid, new ByteArrayResource(imageBytes), "image/png");
            }

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Send mail failed", e);
        }
    }

    public String buildMomoMailContent(TicketEntity ticket, MomoPaymentResult momoResult, String qrCid) {
        String bookingTime = ticket.getCreatedAt().format(MAIL_DATE_FORMAT);
        String departureTime = ticket.getTrip().getDepartureTime().format(MAIL_DATE_FORMAT);
        String arrivalTime = ticket.getTrip().getArrivalTime().format(MAIL_DATE_FORMAT);

        String qrImgBlock = (qrCid != null)
                ? "<img src='cid:" + qrCid + "' width='200' height='200' alt='QR MoMo'/>"
                : "<p style='color:red'>Không thể tải mã QR. Vui lòng dùng link phía trên.</p>";

        return """
        <div style='font-family:Arial,sans-serif; max-width:600px; margin:auto;'>
            <p>Xin chào, vé <b>%s</b> đang chờ thanh toán.</p>
        
            <div style='margin:20px 0; padding:16px; border:1px solid #eee; border-radius:8px;'>
                <table style='width:100%%; border-collapse:collapse;'>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Mã vé</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Ngày đặt</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Điểm đi</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Giờ xuất bến</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Điểm đến</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Giờ đến</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Số ghế</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Giá vé</b></td>
                        <td style='padding:8px; color:#ae2070; font-weight:bold;'>%,d VND</td>
                    </tr>
                </table>
            </div>

            <!-- Option 1 -->
            <div style='margin:20px 0; padding:16px; border:1px solid #eee; border-radius:8px;'>
                <h3 style='margin:0 0 12px;'>Cách 1: Nhấn link thanh toán</h3>
                <a href='%s'
                    style='display:inline-block; padding:12px 28px; background:#ae2070;
                    color:white; border-radius:6px; text-decoration:none; font-weight:bold;
                    font-size:16px;'>
                    Thanh toán ngay qua MoMo
                </a>
            </div>

            <!-- Option 2 -->
            <div style='margin:20px 0; padding:16px; border:1px solid #eee; border-radius:8px;'>
                <h3 style='margin:0 0 8px;'>Cách 2: Quét mã QR</h3>
                <p style='color:#666; font-size:13px; margin:0 0 12px;'>
                    Mở app MoMo → Quét mã → Xác nhận thanh toán
                </p>
                %s
            </div>

            <p style='color:#999; font-size:12px; margin-top:24px;'>
                Link & mã QR có hiệu lực trong <b>15 phút</b>.<br/>
                Nếu cần hỗ trợ, liên hệ hotline VietBus: 0977751951.
            </p>
        </div>
        """.formatted(
                ticket.getTicketCode(),
                ticket.getTicketCode(),
                bookingTime,
                ticket.getTrip().getRoute().getFromStation().getName(),
                departureTime,
                ticket.getTrip().getRoute().getToStation().getName(),
                arrivalTime,
                ticket.getTripSeat().getSeat().getSeatNumber(),
                ticket.getPrice().longValue(),
                momoResult.getPayUrl(),
                qrImgBlock
        );
    }

    public byte[] generateQrAsBytes(String qrContent) {
        try {
            int size = 300;
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(
                    qrContent,
                    BarcodeFormat.QR_CODE,
                    size, size,
                    Map.of(EncodeHintType.MARGIN, 1)
            );
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
            return out.toByteArray();

        } catch (Exception e) {
            log.warn("[Mail] Cannot generate QR: {}", e.getMessage());
            return null;
        }
    }

    public String buildVNPayMailContent(TicketEntity ticket, VNPayPaymentResult vnpayResult) {
        String bookingTime = ticket.getCreatedAt().format(MAIL_DATE_FORMAT);
        String departureTime = ticket.getTrip().getDepartureTime().format(MAIL_DATE_FORMAT);
        String arrivalTime = ticket.getTrip().getArrivalTime().format(MAIL_DATE_FORMAT);

        return """
        <div style='font-family:Arial,sans-serif; max-width:600px; margin:auto;'>
            <p>Xin chào, vé <b>%s</b> đang chờ thanh toán.</p>

            <div style='margin:20px 0; padding:16px; border:1px solid #eee; border-radius:8px;'>
                <table style='width:100%%; border-collapse:collapse;'>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Mã vé</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Ngày đặt</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Điểm đi</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Giờ xuất bến</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Điểm đến</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Giờ đến</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Số ghế</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Giá vé</b></td>
                        <td style='padding:8px; color:#e53935; font-weight:bold;'>%,d VND</td>
                    </tr>
                </table>
            </div>

            <div style='margin:20px 0; padding:16px; border:1px solid #eee; border-radius:8px;'>
                <h3 style='margin:0 0 12px;'>Nhấn link để thanh toán qua VNPay</h3>
                <a href='%s'
                    style='display:inline-block; padding:12px 28px; background:#e53935;
                    color:white; border-radius:6px; text-decoration:none; font-weight:bold;
                    font-size:16px;'>
                    Thanh toán ngay qua VNPay
                </a>
                <p style='color:#999; font-size:12px; margin:12px 0 0;'>
                    Nếu nút không hoạt động, copy link sau vào trình duyệt:<br/>
                    <a href='%s' style='color:#e53935;'>%s</a>
                </p>
            </div>

            <p style='color:#999; font-size:12px; margin-top:24px;'>
                Link thanh toán có hiệu lực trong <b>15 phút</b>.<br/>
                Nếu cần hỗ trợ, liên hệ hotline VietBus: 0977751951.
            </p>
        </div>
        """.formatted(
                ticket.getTicketCode(),
                ticket.getTicketCode(),
                bookingTime,
                ticket.getTrip().getRoute().getFromStation().getName(),
                departureTime,
                ticket.getTrip().getRoute().getToStation().getName(),
                arrivalTime,
                ticket.getTripSeat().getSeat().getSeatNumber(),
                ticket.getPrice().longValue(),
                vnpayResult.getPayUrl(),
                vnpayResult.getPayUrl(),
                vnpayResult.getPayUrl()
        );
    }

    public String buildCancelTicketMailContent(TicketEntity ticket) {
        String bookingTime = ticket.getCreatedAt().format(MAIL_DATE_FORMAT);
        String departureTime = ticket.getTrip().getDepartureTime().format(MAIL_DATE_FORMAT);
        String arrivalTime = ticket.getTrip().getArrivalTime().format(MAIL_DATE_FORMAT);

        return """
        <div style='font-family:Arial,sans-serif; max-width:600px; margin:auto;'>
            <p>Xin chào, vé <b>%s</b> của bạn đã được hủy.</p>
        
            <div style='margin:20px 0; padding:16px; border:1px solid #eee; border-radius:8px;'>
                <table style='width:100%%; border-collapse:collapse;'>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Mã vé</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Ngày đặt</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Tuyến</b></td>
                        <td style='padding:8px;'>%s → %s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Giờ xuất bến</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Giờ đến</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Ghế</b></td>
                        <td style='padding:8px;'>%s</td>
                    </tr>
                    <tr>
                        <td style='padding:8px; background:#f5f5f5;'><b>Số tiền</b></td>
                        <td style='padding:8px; color:#e53935; font-weight:bold;'>%,d VND</td>
                    </tr>
                </table>
            </div>

            <div style='margin:20px 0; padding:16px; border:1px solid #eee; border-radius:8px; background:#fff8f8;'>
                <p style='margin:0; color:#666;'>
                    Vé này không còn hiệu lực để check-in hoặc sử dụng cho chuyến đi.
                </p>
            </div>

            <p style='color:#999; font-size:12px; margin-top:24px;'>
                Nếu bạn không thực hiện yêu cầu hủy vé hoặc cần hỗ trợ, vui lòng liên hệ hotline VietBus: 0977751951.
            </p>

            <p>Cảm ơn bạn đã sử dụng VietBus!</p>
        </div>
        """.formatted(
                ticket.getTicketCode(),
                ticket.getTicketCode(),
                bookingTime,
                ticket.getTrip().getRoute().getFromStation().getName(),
                ticket.getTrip().getRoute().getToStation().getName(),
                departureTime,
                arrivalTime,
                ticket.getTripSeat().getSeat().getSeatNumber(),
                ticket.getPrice().longValue()
        );
    }

    public String buildTripReminderMailContent(TicketEntity ticket) {

        String departureTime = ticket.getTrip().getDepartureTime().format(MAIL_DATE_FORMAT);
        String arrivalTime = ticket.getTrip().getArrivalTime().format(MAIL_DATE_FORMAT);

        return """
    <div style='font-family:Arial,sans-serif; max-width:600px; margin:auto;'>
        <p>
            Xin chào,
        </p>
        <p>
            Đây là email nhắc nhở từ <b>VietBus</b>.
            Chuyến xe của bạn sẽ khởi hành trong khoảng <b>2 giờ nữa</b>.
            Vui lòng chuẩn bị trước khi đến bến.
        </p>

        <div style='margin:20px 0; padding:16px; border:1px solid #eee; border-radius:8px;'>

            <table style='width:100%%; border-collapse:collapse;'>

                <tr>
                    <td style='padding:8px;background:#f5f5f5;'><b>Mã vé</b></td>
                    <td style='padding:8px;'>%s</td>
                </tr>

                <tr>
                    <td style='padding:8px;background:#f5f5f5;'><b>Điểm đi</b></td>
                    <td style='padding:8px;'>%s</td>
                </tr>

                <tr>
                    <td style='padding:8px;background:#f5f5f5;'><b>Giờ xuất bến</b></td>
                    <td style='padding:8px;color:#1976d2;font-weight:bold;'>%s</td>
                </tr>

                <tr>
                    <td style='padding:8px;background:#f5f5f5;'><b>Điểm đến</b></td>
                    <td style='padding:8px;'>%s</td>
                </tr>

                <tr>
                    <td style='padding:8px;background:#f5f5f5;'><b>Giờ đến dự kiến</b></td>
                    <td style='padding:8px;'>%s</td>
                </tr>

                <tr>
                    <td style='padding:8px;background:#f5f5f5;'><b>Số ghế</b></td>
                    <td style='padding:8px;'>%s</td>
                </tr>

                <tr>
                    <td style='padding:8px;background:#f5f5f5;'><b>Giá vé</b></td>
                    <td style='padding:8px;color:#1976d2;font-weight:bold;'>%,d VND</td>
                </tr>

            </table>

        </div>

        <div style='margin:24px 0; text-align:center;'>

            <h3 style='margin-bottom:12px;'>QR Check-in</h3>

            <img src="cid:ticketQr"
                 width="250"
                 style='border:1px solid #ddd;
                        padding:8px;
                        border-radius:8px;
                        background:white;'/>

            <p style='color:#777;font-size:13px;'>
                Xuất trình mã QR này cho nhân viên khi lên xe.
            </p>

        </div>

        <div style='padding:16px;
                    background:#FFF8E1;
                    border-left:4px solid #FFA000;
                    border-radius:8px;'>

            <h3 style='margin-top:0;'>Lưu ý</h3>

            <ul style='line-height:1.8;'>

                <li>Có mặt tại bến trước giờ khởi hành ít nhất <b>15 phút</b>.</li>

                <li>Chuẩn bị CCCD hoặc giấy tờ tùy thân khi cần.</li>

                <li>Giữ điện thoại còn pin để xuất trình mã QR.</li>

                <li>Nếu không thể tham gia chuyến đi, vui lòng liên hệ VietBus sớm để được hỗ trợ.</li>

            </ul>

        </div>

        <p style='margin-top:24px;'>

            Chúc bạn có một chuyến đi an toàn và thuận lợi.

        </p>

        <hr style='margin:24px 0;'>

        <p style='font-size:12px;color:#777;'>

            Hotline: <b>0977751951</b><br>

            Email này được gửi tự động trước giờ khởi hành.

        </p>

    </div>
    """.formatted(
                ticket.getTicketCode(),
                ticket.getTrip().getRoute().getFromStation().getName(),
                departureTime,
                ticket.getTrip().getRoute().getToStation().getName(),
                arrivalTime,
                ticket.getTripSeat().getSeat().getSeatNumber(),
                ticket.getPrice().longValue()
        );
    }

    private static final DateTimeFormatter MAIL_DATE_FORMAT = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");
}
