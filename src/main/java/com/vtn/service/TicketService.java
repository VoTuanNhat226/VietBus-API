package com.vtn.service;

import com.vtn.dto.request.TicketRequest;
import com.vtn.dto.response.TicketResponse;
import com.vtn.entity.*;
import com.vtn.enumdef.PaymentMethodEnum;
import com.vtn.enumdef.PaymentStatusEnum;
import com.vtn.enumdef.TicketStatusEnum;
import com.vtn.enumdef.TripSeatStatusEnum;
import com.vtn.repository.*;
import com.vtn.service.Mail.MailService;
import com.vtn.utils.BaseResponse;
import com.vtn.utils.CodeGeneratorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TicketService {
    // Constant
    private static final String PAY_NOW = "PAY_NOW";

    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final TripSeatRepository tripSeatRepository;
    private final PassengerRepository passengerRepository;
    private final PaymentRepository paymentRepository;
    private final MailService mailService;

    @Autowired
    public TicketService(
            TicketRepository ticketRepository,
            TripRepository tripRepository,
            TripSeatRepository tripSeatRepository,
            PassengerRepository passengerRepository,
            PaymentRepository paymentRepository,
            MailService mailService) {
        this.ticketRepository = ticketRepository;
        this.tripRepository = tripRepository;
        this.tripSeatRepository = tripSeatRepository;
        this.passengerRepository = passengerRepository;
        this.paymentRepository = paymentRepository;
        this.mailService = mailService;
    }

    public BaseResponse getAllTicketsUnpaid(TicketRequest request) {
        List<TicketResponse> tickets = ticketRepository.getAllByCondition(
                        request.getTicketCode(),
                        request.getTripCode(),
                        request.getTicketPaymentType(),
                        request.getTicketSoldBy())
                .stream()
                .map(t -> new TicketResponse(
                        t.getTicketId(),
                        t.getTicketCode(),
                        t.getPrice(),
                        t.getStatus(),
                        t.getPaymentType(),
                        t.getSoldBy(),
                        t.getSoldAt(),
                        t.getTrip().getTripCode(),
                        t.getTrip().getRoute().getFromStation().getName(),
                        t.getTrip().getRoute().getToStation().getName(),
                        t.getTripSeat().getSeat().getSeatNumber()
                ))
                .toList();
        return new BaseResponse(200,tickets,"Get all tickets unpaid successful", null,null);
    }

    public BaseResponse getAllTicketsByTripId(TicketRequest request) {
        List<TicketResponse> tickets = ticketRepository.getAllByTripId(
                        request.getTripId())
                .stream()
                .map(t -> new TicketResponse(
                        t.getTicketId(),
                        t.getTicketCode(),
                        t.getPrice(),
                        t.getStatus(),
                        t.getPaymentType(),
                        t.getSoldBy(),
                        t.getSoldAt(),
                        t.getTrip().getTripCode(),
                        t.getTrip().getRoute().getFromStation().getName(),
                        t.getTrip().getRoute().getToStation().getName(),
                        t.getTripSeat().getSeat().getSeatNumber()
                ))
                .toList();
        return new BaseResponse(200,tickets,"Get all tickets by tripId successful", null,null);
    }

    @Transactional
    public BaseResponse createTicket(TicketRequest request) {
        UserDetails info = getInfo();

        TripEntity trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        TripSeatEntity tripSeat = tripSeatRepository.findById(request.getTripSeatId())
                .orElseThrow(() -> new RuntimeException("Trip seat not found"));

        if (!TripSeatStatusEnum.AVAILABLE.equals(tripSeat.getStatus())) {
            return new BaseResponse(404,null,"Seat have been hold or sold",null,null);
        }

        PassengerEntity passenger = null;
        if (request.getPassengerId() != null) {
            passenger = passengerRepository.findById(request.getPassengerId())
                    .orElseThrow(() -> new RuntimeException("Passenger not found"));
        }
        String passengerEmail = passenger != null ? passenger.getEmail() : null;


        //Generate ticketCode
        String ticketCode;
        do {
            ticketCode = CodeGeneratorUtil.generateCode();
        } while (ticketRepository.existsByTicketCode(ticketCode));

        TicketEntity ticket = new TicketEntity();
        ticket.setTicketCode(ticketCode);
        ticket.setTrip(trip);
        ticket.setTripSeat(tripSeat);
        ticket.setPassenger(passenger);
        ticket.setPrice(request.getTicketPrice());
        ticket.setNote(request.getNote());
        ticket.setPaymentType(request.getPaymentType());
        ticket.setSoldBy(info.getUsername());
        ticket.setSoldAt(LocalDateTime.now());
        ticket.setCreatedBy(info.getUsername());
        ticket.setCreatedAt(LocalDateTime.now());

        if (PAY_NOW.equals(request.getPaymentType())) {
            ticket.setStatus(TicketStatusEnum.PAID);
            tripSeat.setStatus(TripSeatStatusEnum.SOLD);
        } else {
            // PAY_LATER
            ticket.setStatus(TicketStatusEnum.UNPAID);
            tripSeat.setStatus(TripSeatStatusEnum.HOLD);
        }

        tripSeatRepository.save(tripSeat);
        ticketRepository.save(ticket);

        if (PAY_NOW.equals(request.getPaymentType())) {
            PaymentEntity payment = buildPaymentEntity(ticket, request.getPaymentMethod(), info.getUsername());
            paymentRepository.save(payment);

            //Send Mail
            if (passengerEmail != null) {
                String subject = "Xác nhận đặt vé xe VietBus";
                String content = buildTicketMailContent(ticket);

                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                mailService.sendMail(
                                        passengerEmail,
                                        subject,
                                        content
                                );
                            }
                        }
                );
            }
        }

        TicketResponse ticketResponse = new TicketResponse(
                ticket.getTicketId(),
                ticket.getTicketCode(),
                ticket.getPrice(),
                ticket.getStatus(),
                ticket.getPaymentType(),
                ticket.getSoldBy(),
                ticket.getSoldAt(),
                ticket.getTrip().getTripCode(),
                ticket.getTrip().getRoute().getFromStation().getName(),
                ticket.getTrip().getRoute().getToStation().getName(),
                ticket.getTripSeat().getSeat().getSeatNumber());

        return new BaseResponse(201, ticketResponse, "Create ticket successful", null, null);
    }

    @Transactional
    public BaseResponse updateTicket(TicketRequest request) {
        UserDetails info = getInfo();

        TicketEntity ticket = ticketRepository.findByTicketCode(request.getTicketCode());
        if (ticket == null) {
            return new BaseResponse(404,null,"Ticket not found",null,null);
        }

        TripEntity trip = tripRepository.findByTripCode(request.getTripCode());
        if (trip == null) {
            return new BaseResponse(404,null,"Trip not found",null,null);
        }

        TicketStatusEnum currentTicketStatus = ticket.getStatus();
        if (TicketStatusEnum.PAID.equals(currentTicketStatus)) {
            return new BaseResponse(400, null, "Ticket have been paid", null, null);
        }

        if(ticket.getStatus().equals(request.getTicketStatus())) {
            return new BaseResponse(400, null, "No status change", null, null);
        }

        if(TicketStatusEnum.UNPAID.equals(currentTicketStatus) && TicketStatusEnum.PAID.equals(request.getTicketStatus())) {
            ticket.setStatus(request.getTicketStatus());
            ticketRepository.save(ticket);

            TripSeatEntity tripSeat = ticket.getTripSeat();
            tripSeat.setStatus(TripSeatStatusEnum.SOLD);
            tripSeatRepository.save(tripSeat);

            PaymentEntity payment = buildPaymentEntity(ticket, request.getPaymentMethod(), info.getUsername());
            paymentRepository.save(payment);

            return new BaseResponse(200, null, "Update ticket successful, created payment", null, null);
        }
        return new BaseResponse(200, null, "Update ticket successful", null, null);
    }

    // helper
    private PaymentEntity buildPaymentEntity(TicketEntity ticket, PaymentMethodEnum paymentMethod, String createdBy) {
        PaymentEntity payment = new PaymentEntity();
        payment.setTicket(ticket);
        payment.setAmount(ticket.getPrice());
        payment.setMethod(paymentMethod);
        payment.setStatus(PaymentStatusEnum.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        payment.setCreatedBy(createdBy);
        payment.setCreatedAt(LocalDateTime.now());
        return payment;
    };

    private String buildTicketMailContent(TicketEntity ticket) {
        String bookingTime = ticket.getCreatedAt().format(MAIL_DATE_FORMAT);
        String departureTime = ticket.getTrip().getDepartureTime().format(MAIL_DATE_FORMAT);
        String arrivalTime = ticket.getTrip().getArrivalTime().format(MAIL_DATE_FORMAT);

        return """
            Xin chào,
            Vé xe của bạn đã được đặt thành công.
            Mã vé: %s
            Ngày đặt: %s
            Điểm đi: %s
            Thời gian xuất bến: %s
            Điểm đến: %s
            Thời gian đến(dự kiến): %s

            Cảm ơn bạn đã sử dụng VietBus!
            """.formatted(
                ticket.getTicketCode(),
                bookingTime,
                ticket.getTrip().getRoute().getFromStation().getName(),
                departureTime,
                ticket.getTrip().getRoute().getToStation().getName(),
                arrivalTime
        );
    }

    private static final DateTimeFormatter MAIL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");

    private UserDetails getInfo() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
