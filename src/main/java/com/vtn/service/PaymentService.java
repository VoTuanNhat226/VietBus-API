package com.vtn.service;

import com.vtn.dto.request.PaymentRequest;
import com.vtn.dto.response.PaymentResponse;
import com.vtn.repository.PaymentRepository;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public BaseResponse getAllPayments(PaymentRequest request) {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
