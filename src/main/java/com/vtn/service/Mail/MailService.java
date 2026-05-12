package com.vtn.service.Mail;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
}
