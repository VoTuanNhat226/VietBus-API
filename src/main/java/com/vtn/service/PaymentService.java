package com.vtn.service;

import com.vtn.dto.request.PaymentRequest;
import com.vtn.dto.response.PaymentResponse;
import com.vtn.entity.PaymentEntity;
import com.vtn.entity.TicketEntity;
import com.vtn.entity.TripSeatEntity;
import com.vtn.enumdef.PaymentMethodEnum;
import com.vtn.enumdef.PaymentStatusEnum;
import com.vtn.enumdef.TicketStatusEnum;
import com.vtn.enumdef.TripSeatStatusEnum;
import com.vtn.repository.PaymentRepository;
import com.vtn.repository.TicketRepository;
import com.vtn.repository.TripSeatRepository;
import com.vtn.service.Mail.MailService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final TripSeatRepository tripSeatRepository;
    private final MailService mailService;
    private final TicketService ticketService;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          TicketRepository ticketRepository,
                          TripSeatRepository tripSeatRepository,
                          MailService mailService,
                          TicketService ticketService) {
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
        this.tripSeatRepository = tripSeatRepository;
        this.mailService = mailService;
        this.ticketService = ticketService;
    }

    public BaseResponse getAllPayments(PaymentRequest request) {
        List<PaymentResponse> payments = paymentRepository.getAllByCondition(
                        request.getMethod(),
                        request.getStatus(),
                        request.getTicketCode(),
                        request.getTicketStatus(),
                        request.getTicketPaymentType())
                .stream()
                .map(p -> new PaymentResponse(
                        p.getPaymentId(),
                        p.getMethod(),
                        p.getStatus(),
                        p.getAmount(),
                        p.getPaidAt(),
                        p.getTicket().getTicketId(),
                        p.getTicket().getTicketCode(),
                        p.getTicket().getPrice(),
                        p.getTicket().getStatus(),
                        p.getTicket().getPaymentType()
                ))
                .toList();
        return new BaseResponse(200, payments,null,null,null);
    }

    @Transactional
    public void confirmPayment(String ticketCode, String transactionId, PaymentMethodEnum method) throws Exception {
        TicketEntity ticket = ticketRepository.findByTicketCode(ticketCode);
        if (ticket == null || ticket.getStatus() == TicketStatusEnum.PAID) return;

        // Update status ticket UNPAID -> PAID
        ticket.setStatus(TicketStatusEnum.PAID);
        ticket.setTransactionId(transactionId);
        ticket.setPaymentMethod(method);
        ticket.setPaidAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        // Update status seat HOLD -> SOLD
        TripSeatEntity tripSeat = tripSeatRepository.findByTripSeatId(ticket.getTripSeat().getId());
        tripSeat.setStatus(TripSeatStatusEnum.SOLD);
        tripSeatRepository.save(tripSeat);

        // Create Payment
        PaymentEntity payment = new PaymentEntity();
        payment.setTicket(ticket);
        payment.setAmount(ticket.getPrice());
        payment.setMethod(method);
        payment.setStatus(PaymentStatusEnum.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Send ticket mail after payment success
         ticketService.sendTicketMailAfterCommit(ticket);
    }
}
