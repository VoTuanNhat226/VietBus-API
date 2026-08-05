package com.vtn.consumer;

import com.vtn.constant.Constants;
import com.vtn.dto.event.TicketMailEvent;
import com.vtn.dto.result.MomoPaymentResult;
import com.vtn.dto.result.VNPayPaymentResult;
import com.vtn.entity.PassengerEntity;
import com.vtn.entity.TicketEntity;
import com.vtn.repository.TicketRepository;
import com.vtn.service.Mail.MailService;
import com.vtn.service.QR.QrCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailEventConsumer {

    private final TicketRepository ticketRepository;
    private final MailService mailService;
    private final QrCodeService qrCodeService;

    @KafkaListener(topics = Constants.TOPIC_TICKET_MAIL_EVENTS, groupId = "vietbus-mail-service")
    @Transactional(readOnly = true)
    public void onMailEvent(TicketMailEvent event) throws Exception {
        log.info("[Kafka] Received mail event ticketId={}, type={}", event.getTicketId(), event.getType());

        TicketEntity ticket = ticketRepository.findByTicketIdWithDetails(event.getTicketId());
        if (ticket == null) {
            log.warn("[Kafka] Ticket not found, skip mail event: {}", event.getTicketId());
            return;
        }

        PassengerEntity passenger = ticket.getPassenger();
        if (passenger == null || passenger.getEmail() == null) {
            log.info("[Kafka] Ticket {} has no passenger email, skip mail", ticket.getTicketCode());
            return;
        }

        switch (event.getType()) {
            case TICKET_CONFIRMATION -> sendTicketConfirmation(ticket, passenger.getEmail());
            case TICKET_CANCELLATION -> sendTicketCancellation(ticket, passenger.getEmail());
            case MOMO_PAYMENT_REQUEST -> sendMomoPaymentRequest(ticket, passenger.getEmail());
            case VNPAY_PAYMENT_REQUEST -> sendVNPayPaymentRequest(ticket, passenger.getEmail());
            case TRIP_REMINDER -> sendTripReminder(ticket, passenger.getEmail());
        }
    }

    private void sendTicketConfirmation(TicketEntity ticket, String email) throws Exception {
        byte[] qr = qrCodeService.generateQrCode(ticket.getTicketCode());
        String subject = "XÁC NHẬN ĐẶT VÉ XE VIETBUS";
        String content = mailService.buildTicketMailContent(ticket);
        mailService.sendTicketMail(email, subject, content, qr);
    }

    private void sendTicketCancellation(TicketEntity ticket, String email) {
        String subject = "XÁC NHẬN HỦY VÉ XE VIETBUS";
        String content = mailService.buildCancelTicketMailContent(ticket);
        mailService.sendHtmlMail(email, subject, content, null, null);
    }

    private void sendMomoPaymentRequest(TicketEntity ticket, String email) {
        MomoPaymentResult momoResult = MomoPaymentResult.builder()
                .payUrl(ticket.getMomoPayUrl())
                .qrCodeUrl(ticket.getMomoQrCode())
                .orderId(ticket.getTicketCode())
                .build();

        byte[] qrBytes = mailService.generateQrAsBytes(momoResult.getQrCodeUrl());
        String qrCid = "qr-momo-" + ticket.getTicketCode();
        String subject = "THANH TOÁN VÉ XE VIETBUS - MOMO";
        String content = mailService.buildMomoMailContent(ticket, momoResult, qrBytes != null ? qrCid : null);

        mailService.sendHtmlMail(email, subject, content, qrBytes, qrCid);
    }

    private void sendVNPayPaymentRequest(TicketEntity ticket, String email) {
        VNPayPaymentResult vnpayResult = VNPayPaymentResult.builder()
                .payUrl(ticket.getVnpayPayUrl())
                .txnRef(ticket.getTicketCode())
                .build();

        String subject = "THANH TOÁN VÉ XE VIETBUS - VNPAY";
        String content = mailService.buildVNPayMailContent(ticket, vnpayResult);

        mailService.sendHtmlMail(email, subject, content, null, null);
    }

    private void sendTripReminder(TicketEntity ticket, String email) {
        byte[] qr = mailService.generateQrAsBytes(ticket.getTicketCode());
        String subject = "NHẮC NHỞ CHUYẾN ĐI CỦA BẠN";
        String content = mailService.buildTripReminderMailContent(ticket);
        mailService.sendHtmlMail(email, subject, content, qr, "ticketQr");
    }
}
