package com.vtn.service;

import com.vtn.dto.request.TicketRequest;
import com.vtn.dto.response.TicketResponse;
import com.vtn.dto.result.MomoPaymentResult;
import com.vtn.entity.*;
import com.vtn.enumdef.*;
import com.vtn.repository.*;
import com.vtn.service.Mail.MailService;
import com.vtn.service.Momo.MomoService;
import com.vtn.service.QR.QrCodeService;
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
import java.util.Comparator;
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
    private final QrCodeService qrCodeService;
    private final MomoService momoService;

    @Autowired
    public TicketService(
            TicketRepository ticketRepository,
            TripRepository tripRepository,
            TripSeatRepository tripSeatRepository,
            PassengerRepository passengerRepository,
            PaymentRepository paymentRepository,
            MailService mailService,
            QrCodeService qrCodeService,
            MomoService momoService) {
        this.ticketRepository = ticketRepository;
        this.tripRepository = tripRepository;
        this.tripSeatRepository = tripSeatRepository;
        this.passengerRepository = passengerRepository;
        this.paymentRepository = paymentRepository;
        this.mailService = mailService;
        this.qrCodeService = qrCodeService;
        this.momoService = momoService;
    }

    public BaseResponse getAllTickets(TicketRequest request) {
        List<TicketResponse> tickets = ticketRepository.getAllTicketByCondition(
                        request.getTicketCode(),
                        request.getTicketStatus(),
                        request.getTripCode(),
                        request.getTripId(),
                        request.getTicketPaymentType(),
                        request.getPassengerName(),
                        request.getPassengerPhoneNumber())
                .stream()
                .map(this::toTicketResponse)
                .sorted(Comparator.comparing(TicketResponse::getTicketSoldAt).reversed())
                .toList();

        return new BaseResponse(200, tickets, "Get all tickets successful", null, null);
    }


    public BaseResponse getAllTicketsUnpaid(TicketRequest request) {
        List<TicketResponse> tickets = ticketRepository.getAllTicketUnpaid(
                        request.getTicketCode(),
                        request.getTripCode(),
                        request.getTicketPaymentType(),
                        request.getTicketSoldBy())
                .stream()
                .map(this::toTicketResponse)
                .toList();

        return new BaseResponse(200, tickets, "Get all tickets unpaid successful", null, null);
    }


    public BaseResponse getAllTicketsByTripId(TicketRequest request) {
        List<TicketResponse> tickets = ticketRepository.getAllByTripId(request.getTripId())
                .stream()
                .map(this::toTicketResponse)
                .toList();

        return new BaseResponse(200, tickets, "Get all tickets by tripId successful", null, null);
    }

    @Transactional
    public BaseResponse createTicket(TicketRequest request) throws Exception {
        UserDetails info = getInfo();

        // Validate Input
        TripEntity trip = tripRepository.findById(request.getTripId()).orElseThrow(() -> new RuntimeException("Trip not found"));

        TripSeatEntity tripSeat = tripSeatRepository.findById(request.getTripSeatId()).orElseThrow(() -> new RuntimeException("Trip seat not found"));

        if (!TripSeatStatusEnum.AVAILABLE.equals(tripSeat.getStatus())) {
            return new BaseResponse(409, null, "Seat have been hold or sold", null, null);
        }

        PassengerEntity passenger = null;
        if (request.getPassengerId() != null) {
            passenger = passengerRepository.findById(request.getPassengerId()).orElseThrow(() -> new RuntimeException("Passenger not found"));
        }

        // Generate ticketCode
        String ticketCode = generateUniqueTicketCode();

        boolean isPayNow = PaymentTypeEnum.PAY_NOW.equals(request.getPaymentType());
        boolean isPayLater = PaymentTypeEnum.PAY_LATER.equals(request.getPaymentType());

        TicketEntity ticket = buildTicketEntity(request, trip, tripSeat, passenger, ticketCode, info.getUsername(), isPayNow);
        tripSeat.setStatus(isPayNow ? TripSeatStatusEnum.SOLD : TripSeatStatusEnum.HOLD);

        tripSeatRepository.save(tripSeat);
        ticketRepository.save(ticket);

        if (isPayNow) {
            PaymentEntity payment = buildPaymentEntity(ticket, request.getPaymentMethod(), info.getUsername());
            paymentRepository.save(payment);
            sendTicketMailAfterCommit(ticket);
        }

        if (isPayLater) {
            if (PaymentMethodEnum.MOMO.equals(request.getPaymentMethod())) {
                MomoPaymentResult momoResult = momoService.createPayment(
                        ticket.getTicketCode(),
                        ticket.getPrice()
                );

                ticket.setMomoPayUrl(momoResult.getPayUrl());
                ticket.setMomoQrCode(momoResult.getQrCodeUrl());
                ticket.setPaymentMethod(PaymentMethodEnum.MOMO);
                ticketRepository.save(ticket);

                sendMomoQrMailAfterCommit(ticket, momoResult);
            }
        }

        return new BaseResponse(201, toTicketResponse(ticket), "Create ticket successful, created payment", null, null);
    }

    @Transactional
    public BaseResponse updateTicket(TicketRequest request) throws Exception {
        UserDetails info = getInfo();

        // Validate Input
        TicketEntity ticket = ticketRepository.findByTicketCode(request.getTicketCode());
        if (ticket == null) {
            return new BaseResponse(404, null, "Ticket not found", null, null);
        }

        TicketStatusEnum currentStatus = ticket.getStatus();
        if (TicketStatusEnum.PAID.equals(currentStatus)) {
            return new BaseResponse(409, null, "Ticket have been paid", null, null);
        }

        if (currentStatus.equals(request.getTicketStatus())) {
            return new BaseResponse(400, null, "No status change", null, null);
        }

        if (TicketStatusEnum.UNPAID.equals(currentStatus) && TicketStatusEnum.PAID.equals(request.getTicketStatus())) {
            ticket.setStatus(TicketStatusEnum.PAID);
            ticketRepository.save(ticket);

            TripSeatEntity tripSeat = ticket.getTripSeat();
            tripSeat.setStatus(TripSeatStatusEnum.SOLD);
            tripSeatRepository.save(tripSeat);

            PaymentEntity payment = buildPaymentEntity(ticket, request.getPaymentMethod(), info.getUsername());
            paymentRepository.save(payment);

            sendTicketMailAfterCommit(ticket);

            return new BaseResponse(200, null, "Update ticket successful, created payment", null, null);
        }

        return new BaseResponse(200, null, "Update ticket successful", null, null);
    }

    // ------------------ helper ------------------
    private TicketResponse toTicketResponse(TicketEntity t) {
        PassengerEntity passenger = t.getPassenger();
        return new TicketResponse(
                t.getTicketId(),
                t.getTicketCode(),
                t.getPrice(),
                t.getStatus(),
                t.getPaymentType(),
                t.getSoldBy(),
                t.getSoldAt(),
                t.getNote(),
                t.getTrip().getTripId(),
                t.getTrip().getTripCode(),
                t.getTrip().getRoute().getFromStation().getName(),
                t.getTrip().getRoute().getToStation().getName(),
                t.getTripSeat().getSeat().getSeatNumber(),
                passenger != null ? passenger.getFullName() : null,
                passenger != null ? passenger.getPhoneNumber() : null,
                passenger != null ? passenger.getNote() : null
        );
    }

    private TicketEntity buildTicketEntity(
            TicketRequest request,
            TripEntity trip,
            TripSeatEntity tripSeat,
            PassengerEntity passenger,
            String ticketCode,
            String username,
            boolean isPayNow) {

        TicketEntity ticket = new TicketEntity();
        ticket.setTicketCode(ticketCode);
        ticket.setTrip(trip);
        ticket.setTripSeat(tripSeat);
        ticket.setPassenger(passenger);
        ticket.setPrice(request.getTicketPrice());
        ticket.setNote(request.getNote());
        ticket.setPaymentType(request.getPaymentType());
        ticket.setSoldBy(username);
        ticket.setSoldAt(LocalDateTime.now());
        ticket.setCreatedBy(username);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setStatus(isPayNow ? TicketStatusEnum.PAID : TicketStatusEnum.UNPAID);
        return ticket;
    }

    private PaymentEntity buildPaymentEntity(TicketEntity ticket, PaymentMethodEnum method, String createdBy) {
        PaymentEntity payment = new PaymentEntity();
        payment.setTicket(ticket);
        payment.setAmount(ticket.getPrice());
        payment.setMethod(method);
        payment.setStatus(PaymentStatusEnum.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        payment.setCreatedBy(createdBy);
        payment.setCreatedAt(LocalDateTime.now());
        return payment;
    }

    private void sendTicketMailAfterCommit(TicketEntity ticket) throws Exception {
        PassengerEntity passenger = ticket.getPassenger();
        if (passenger == null || passenger.getEmail() == null) return;

        String passengerEmail = passenger.getEmail();
        byte[] qrCode = qrCodeService.generateQrCode(ticket.getTicketCode());
        String subject = "Xác nhận đặt vé xe VietBus";
        String content = buildTicketMailContent(ticket);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        mailService.sendTicketMail(passengerEmail, subject, content, qrCode);
                    }
                }
        );
    }

    private String buildTicketMailContent(TicketEntity ticket) {
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

    private void sendMomoQrMailAfterCommit(TicketEntity ticket, MomoPaymentResult momoResult)
            throws Exception {

        PassengerEntity passenger = ticket.getPassenger();
        if (passenger == null || passenger.getEmail() == null) return;

        String email   = passenger.getEmail();
        String subject = "Thanh toán vé xe VietBus - " + ticket.getTicketCode();
        String content = buildMomoMailContent(ticket, momoResult);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        mailService.sendHtmlMail(email, subject, content);
                    }
                }
        );
    }

    private String buildMomoMailContent(TicketEntity ticket, MomoPaymentResult momoResult) {
        return """
        <div style='font-family: Arial'>
            <h2>VietBus - Thanh toán vé xe</h2>
            <p>Xin chào, vé <b>%s</b> đang chờ thanh toán.</p>

            <p><b>Tuyến:</b> %s → %s</p>
            <p><b>Ghế:</b> %s</p>
            <p><b>Số tiền:</b> %,d VND</p>

            <h3>Thanh toán qua MoMo</h3>
            <p><a href="%s" style="padding:10px 20px;background:#ae2070;color:white;
               border-radius:5px;text-decoration:none">👉 Thanh toán ngay</a></p>

            <p>Hoặc scan QR:</p>
            <img src="%s" width="200"/>

            <p><i>Link thanh toán có hiệu lực trong 15 phút.</i></p>
        </div>
        """.formatted(
                ticket.getTicketCode(),
                ticket.getTrip().getRoute().getFromStation().getName(),
                ticket.getTrip().getRoute().getToStation().getName(),
                ticket.getTripSeat().getSeat().getSeatNumber(),
                ticket.getPrice().longValue(),
                momoResult.getPayUrl(),
                momoResult.getQrCodeUrl()
        );
    }

    private String generateUniqueTicketCode() {
        String code;
        do {
            code = CodeGeneratorUtil.generateCode();
        } while (ticketRepository.existsByTicketCode(code));
        return code;
    }

    private UserDetails getInfo() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private static final DateTimeFormatter MAIL_DATE_FORMAT = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");
}
