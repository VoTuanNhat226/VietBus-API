package com.vtn.service;

import com.vtn.dto.request.TicketRequest;
import com.vtn.dto.response.TicketResponse;
import com.vtn.dto.event.TicketMailEvent;
import com.vtn.dto.result.MomoPaymentResult;
import com.vtn.dto.result.VNPayPaymentResult;
import com.vtn.entity.*;
import com.vtn.enumdef.*;
import com.vtn.producer.MailEventProducer;
import com.vtn.repository.*;
import com.vtn.service.Momo.MomoService;
import com.vtn.service.Quartz.QuartzService;
import com.vtn.service.VNPay.VNPayService;
import com.vtn.utils.BaseResponse;
import com.vtn.utils.CodeGeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final TripSeatRepository tripSeatRepository;
    private final PassengerRepository passengerRepository;
    private final PaymentRepository paymentRepository;
    private final MailEventProducer mailEventProducer;
    private final MomoService momoService;
    private final VNPayService vnPayService;
    private final QuartzService quartzService;

    @Autowired
    public TicketService(
            TicketRepository ticketRepository,
            TripRepository tripRepository,
            TripSeatRepository tripSeatRepository,
            PassengerRepository passengerRepository,
            PaymentRepository paymentRepository,
            MailEventProducer mailEventProducer,
            MomoService momoService,
            VNPayService vnPayService,
            QuartzService quartzService) {
        this.ticketRepository = ticketRepository;
        this.tripRepository = tripRepository;
        this.tripSeatRepository = tripSeatRepository;
        this.passengerRepository = passengerRepository;
        this.paymentRepository = paymentRepository;
        this.mailEventProducer = mailEventProducer;
        this.momoService = momoService;
        this.vnPayService = vnPayService;
        this.quartzService = quartzService;
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

    public BaseResponse countTicketUnpaid(TicketRequest request) {
        Long count = ticketRepository.countTicketUnpaid();
        return new BaseResponse(200, count, "Count all tickets unpaid successful", null, null);
    }

    @Transactional
    public BaseResponse createTicket(TicketRequest request) throws Exception {
        UserDetails info = getInfo();

        // Validate Input
        TripEntity trip = tripRepository.findById(request.getTripId()).orElseThrow(() -> new RuntimeException("Trip not found"));

        PassengerEntity passenger = null;
        if (request.getPassengerId() != null) {
            passenger = passengerRepository.findById(request.getPassengerId()).orElseThrow(() -> new RuntimeException("Passenger not found"));
        }

        BaseResponse seatsSelectedError = validateSeatsSelected(request);
        if (seatsSelectedError != null) {
            return seatsSelectedError;
        }

        List<TripSeatEntity> tripSeats = tripSeatRepository.findAllById(request.getTripSeatIds());

        BaseResponse seatsExistError = validateSeatsExist(request, tripSeats);
        if (seatsExistError != null) {
            return seatsExistError;
        }

        BaseResponse seatsAvailableError = validateSeatsAvailable(tripSeats, info);
        if (seatsAvailableError != null) {
            return seatsAvailableError;
        }

        // Process create ticket
        boolean isPayNow = PaymentTypeEnum.PAY_NOW.equals(request.getPaymentType());
        boolean isPayLater = PaymentTypeEnum.PAY_LATER.equals(request.getPaymentType());

        for (TripSeatEntity seat : tripSeats) {
            String ticketCode = generateUniqueTicketCode();

            TicketEntity ticket = buildTicketEntity(
                    request,
                    trip,
                    seat,
                    passenger,
                    ticketCode,
                    info.getUsername(),
                    isPayNow
            );

            seat.setStatus(isPayNow ? TripSeatStatusEnum.SOLD : TripSeatStatusEnum.HOLD);

            tripSeatRepository.save(seat);
            ticketRepository.save(ticket);

            if (isPayNow) {
                PaymentEntity payment = buildPaymentEntity(
                        ticket,
                        request.getPaymentMethod(),
                        info.getUsername()
                );

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

                if (PaymentMethodEnum.VNPAY.equals(request.getPaymentMethod())) {
                    VNPayPaymentResult vnPayResult = vnPayService.createPayment(
                            ticket.getTicketCode(),
                            ticket.getPrice(),
                            request.getIpAddress()
                    );

                    ticket.setVnpayPayUrl(vnPayResult.getPayUrl());
                    ticket.setPaymentMethod(PaymentMethodEnum.VNPAY);

                    ticketRepository.save(ticket);
                    sendVNPayMailAfterCommit(ticket, vnPayResult);
                }

                // Job CancelPayLaterTicketl
                quartzService.scheduleCancelTicket(ticket.getTicketId());
            }
        }

        return new BaseResponse(201, null, "Create ticket successful", null, null);
    }

    @Transactional
    public BaseResponse updateTicket(TicketRequest request) throws Exception {
        UserDetails info = getInfo();

        // Validate Input
        TicketEntity ticket = ticketRepository.findByTicketCode(request.getTicketCode());
        BaseResponse ticketExistsError = validateTicketExists(ticket);
        if (ticketExistsError != null) {
            return ticketExistsError;
        }

        BaseResponse updatableError = validateTicketUpdatable(ticket, request);
        if (updatableError != null) {
            return updatableError;
        }

        TicketStatusEnum currentStatus = ticket.getStatus();

        // Process update ticket
        if (TicketStatusEnum.UNPAID.equals(currentStatus) && TicketStatusEnum.PAID.equals(request.getTicketStatus())) {
            ticket.setStatus(TicketStatusEnum.PAID);
            ticket.setUpdatedBy(info.getUsername());
            ticket.setUpdatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);

            TripSeatEntity tripSeat = ticket.getTripSeat();
            tripSeat.setStatus(TripSeatStatusEnum.SOLD);
            tripSeatRepository.save(tripSeat);

            PaymentEntity payment = buildPaymentEntity(ticket, request.getPaymentMethod(), info.getUsername());
            paymentRepository.save(payment);

            sendTicketMailAfterCommit(ticket);

            return new BaseResponse(200, null, "Update ticket successful", null, null);
        }

        return new BaseResponse(200, null, "Update ticket successful", null, null);
    }

    @Transactional
    public BaseResponse cancelTicket(TicketRequest request) throws Exception {
        UserDetails info = getInfo();

        // Validate ticket
        TicketEntity ticket = ticketRepository.findByTicketCode(request.getTicketCode());
        BaseResponse ticketExistsError = validateTicketExists(ticket);
        if (ticketExistsError != null) {
            return ticketExistsError;
        }

        BaseResponse notCancelledError = validateTicketNotCancelled(ticket);
        if (notCancelledError != null) {
            return notCancelledError;
        }

        // Validate trip
        TripEntity trip = ticket.getTrip();
        BaseResponse tripExistsError = validateTripExists(trip);
        if (tripExistsError != null) {
            return tripExistsError;
        }

        BaseResponse tripCancellableError = validateTripCancellable(trip);
        if (tripCancellableError != null) {
            return tripCancellableError;
        }

        // Validate trip seat
        TripSeatEntity tripSeat = ticket.getTripSeat();
        BaseResponse tripSeatExistsError = validateTripSeatExists(tripSeat);
        if (tripSeatExistsError != null) {
            return tripSeatExistsError;
        }

        // Validate payment if ticket status is PAID
        PaymentEntity payment = null;
        if (TicketStatusEnum.PAID.equals(ticket.getStatus())) {
            payment = paymentRepository.getByTicketId(ticket.getTicketId());

            BaseResponse paymentExistsError = validatePaymentExists(payment);
            if (paymentExistsError != null) {
                return paymentExistsError;
            }
        }

        LocalDateTime now = LocalDateTime.now();

        // Update ticket
        ticket.setStatus(TicketStatusEnum.CANCELED);
        ticket.setUpdatedBy(info.getUsername());
        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);

        // Update trip seat
        tripSeat.setStatus(TripSeatStatusEnum.AVAILABLE);
        tripSeatRepository.save(tripSeat);

        // Update payment
        if (payment != null) {
            payment.setStatus(PaymentStatusEnum.REFUNDED);
            payment.setUpdatedBy(info.getUsername());
            payment.setUpdatedAt(now);
            paymentRepository.save(payment);
        }

        // Send mail cancel ticket
        sendCancelTicketMailAfterCommit(ticket);

        return new BaseResponse(200, null, "Cancel ticket successful", null, null);
    }

    // ------------------ validate ------------------
    private BaseResponse validateSeatsSelected(TicketRequest request) {
        if (request.getTripSeatIds() == null || request.getTripSeatIds().isEmpty()) {
            return new BaseResponse(400, null, "Please choose at least one seat", null, null);
        }
        return null;
    }

    private BaseResponse validateSeatsExist(TicketRequest request, List<TripSeatEntity> tripSeats) {
        if (tripSeats.size() != request.getTripSeatIds().size()) {
            return new BaseResponse(404, null, "Some trip seats not found", null, null);
        }
        return null;
    }

    private BaseResponse validateSeatsAvailable(List<TripSeatEntity> tripSeats, UserDetails info) {
        for (TripSeatEntity seat : tripSeats) {
            boolean available = TripSeatStatusEnum.AVAILABLE.equals(seat.getStatus());
            boolean blockedByCurrentUser = TripSeatStatusEnum.BLOCKED.equals(seat.getStatus()) && info.getUsername().equals(seat.getProcessingStaff());
            if (!available && !blockedByCurrentUser) {
                String message = String.format("Seat %s has been hold or sold", seat.getSeat().getSeatNumber());
                return new BaseResponse(409, null, message, null, null);
            }
        }
        return null;
    }

    private BaseResponse validateTicketExists(TicketEntity ticket) {
        if (ticket == null) {
            return new BaseResponse(404, null, "Ticket not found", null, null);
        }
        return null;
    }

    private BaseResponse validateTicketUpdatable(TicketEntity ticket, TicketRequest request) {
        if (TicketStatusEnum.PAID.equals(ticket.getStatus())) {
            return new BaseResponse(409, null, "Ticket have been paid", null, null);
        }
        if (ticket.getStatus().equals(request.getTicketStatus())) {
            return new BaseResponse(400, null, "No status change", null, null);
        }
        return null;
    }

    private BaseResponse validateTicketNotCancelled(TicketEntity ticket) {
        if (TicketStatusEnum.CANCELED.equals(ticket.getStatus())) {
            return new BaseResponse(400, null, "Ticket already cancelled", null, null);
        }
        return null;
    }

    private BaseResponse validateTripExists(TripEntity trip) {
        if (trip == null) {
            return new BaseResponse(404, null, "Trip not found", null, null);
        }
        return null;
    }

    private BaseResponse validateTripCancellable(TripEntity trip) {
        if (!TripStatusEnum.OPEN_FOR_BOOKING.equals(trip.getStatus()) && !TripStatusEnum.CLOSED_FOR_BOOKING.equals(trip.getStatus())) {
            return new BaseResponse(400, null, "Cannot cancel ticket because trip status is invalid", null, null);
        }
        return null;
    }

    private BaseResponse validateTripSeatExists(TripSeatEntity tripSeat) {
        if (tripSeat == null) {
            return new BaseResponse(404, null, "Trip seat not found", null, null);
        }
        return null;
    }

    private BaseResponse validatePaymentExists(PaymentEntity payment) {
        if (payment == null) {
            return new BaseResponse(404, null, "Payment not found", null, null);
        }
        return null;
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
                t.getPaymentMethod(),
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

    public void sendTicketMailAfterCommit(TicketEntity ticket) {
        publishAfterCommit(ticket.getTicketId(), MailEventTypeEnum.TICKET_CONFIRMATION);
    }

    public void sendCancelTicketMailAfterCommit(TicketEntity ticket) {
        publishAfterCommit(ticket.getTicketId(), MailEventTypeEnum.TICKET_CANCELLATION);
    }

    private void sendMomoQrMailAfterCommit(TicketEntity ticket, MomoPaymentResult momoResult) {
        publishAfterCommit(ticket.getTicketId(), MailEventTypeEnum.MOMO_PAYMENT_REQUEST);
    }

    private void sendVNPayMailAfterCommit(TicketEntity ticket, VNPayPaymentResult vnpayResult) {
        publishAfterCommit(ticket.getTicketId(), MailEventTypeEnum.VNPAY_PAYMENT_REQUEST);
    }

    private void publishAfterCommit(UUID ticketId, MailEventTypeEnum type) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        mailEventProducer.sendMailEvent(new TicketMailEvent(ticketId, type, LocalDateTime.now()));
                    }
                }
        );
    }

    private String generateUniqueTicketCode() {
        String code;
        do {
            code = CodeGeneratorUtil.generateTicketCode();
        } while (ticketRepository.existsByTicketCode(code));
        return code;
    }

    private UserDetails getInfo() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
