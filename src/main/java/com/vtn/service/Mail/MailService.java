package com.vtn.service.Mail;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vtn.dto.result.MomoPaymentResult;
import com.vtn.entity.TicketEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async("mailExecutor")
    public void sendTicketMail(String to, String subject, String htmlContent, byte[] qrCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.addInline(
                    "ticketQr",
                    new ByteArrayResource(qrCode),
                    "image/png"
            );

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Send mail failed", e);
        }
    }

    public String buildTicketMailContent(TicketEntity ticket) {
        String bookingTime   = ticket.getCreatedAt().format(MAIL_DATE_FORMAT);
        String departureTime = ticket.getTrip().getDepartureTime().format(MAIL_DATE_FORMAT);
        String arrivalTime   = ticket.getTrip().getArrivalTime().format(MAIL_DATE_FORMAT);

        return """
                <div style='font-family: Arial'>
                    <h2>VietBus - Xác nhận đặt vé</h2>
 
                    <p><b>Mã vé:</b> %s</p>
                    <p><b>Ngày đặt:</b> %s</p>
 
                    <p><b>Điểm đi:</b> %s</p>
                    <p><b>Giờ xuất bến:</b> %s</p>
 
                    <p><b>Điểm đến:</b> %s</p>
                    <p><b>Giờ đến:</b> %s</p>
 
                    <p><b>Ghế:</b> %s</p>
 
                    <h3>QR Check-in</h3>
                    <img src="cid:ticketQr" width="250"/>
 
                    <p>Vui lòng đưa mã QR cho nhân viên khi lên xe.</p>
                    <p>Cảm ơn bạn đã sử dụng VietBus!</p>
                </div>
                """.formatted(
                ticket.getTicketCode(),
                bookingTime,
                ticket.getTrip().getRoute().getFromStation().getName(),
                departureTime,
                ticket.getTrip().getRoute().getToStation().getName(),
                arrivalTime,
                ticket.getTripSeat().getSeat().getSeatNumber()
        );
    }

    @Async("mailExecutor")
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
        String qrImgBlock = (qrCid != null)
                ? "<img src='cid:" + qrCid + "' width='200' height='200' alt='QR MoMo'/>"
                : "<p style='color:red'>Không thể tải mã QR. Vui lòng dùng link phía trên.</p>";

        return """
        <div style='font-family:Arial,sans-serif; max-width:600px; margin:auto;'>
          <p>Xin chào, vé <b>%s</b> đang chờ thanh toán.</p>

          <table style='width:100%%; border-collapse:collapse; margin:16px 0;'>
            <tr><td style='padding:8px; background:#f5f5f5;'><b>Tuyến</b></td>
                <td style='padding:8px;'>%s → %s</td></tr>
            <tr><td style='padding:8px; background:#f5f5f5;'><b>Ghế</b></td>
                <td style='padding:8px;'>%s</td></tr>
            <tr><td style='padding:8px; background:#f5f5f5;'><b>Số tiền</b></td>
                <td style='padding:8px; color:#ae2070; font-weight:bold;'>%,d VND</td></tr>
          </table>

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
            ⏰ Link & mã QR có hiệu lực trong <b>15 phút</b>.<br/>
            Nếu cần hỗ trợ, liên hệ hotline VietBus: 0977751951.
          </p>
        </div>
        """.formatted(
                ticket.getTicketCode(),
                ticket.getTrip().getRoute().getFromStation().getName(),
                ticket.getTrip().getRoute().getToStation().getName(),
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

    private static final DateTimeFormatter MAIL_DATE_FORMAT = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");
}
