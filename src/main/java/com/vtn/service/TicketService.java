package com.vtn.service;

import com.vtn.dto.request.TicketRequest;
import com.vtn.entity.*;
import com.vtn.repository.*;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final TripSeatRepository tripSeatRepository;
    private final PassengerRepository passengerRepository;
    private final PaymentRepository paymentRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 10;
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    public TicketService(
            TicketRepository ticketRepository,
            TripRepository tripRepository,
            TripSeatRepository tripSeatRepository,
            PassengerRepository passengerRepository,
            PaymentRepository paymentRepository) {
        this.ticketRepository = ticketRepository;
        this.tripRepository = tripRepository;
        this.tripSeatRepository = tripSeatRepository;
        this.passengerRepository = passengerRepository;
        this.paymentRepository = paymentRepository;
    }

    private String generateTripCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    @Transactional
    public BaseResponse createTicket(TicketRequest request) {

        UserDetails info = (UserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        TripEntity trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe"));

        TripSeatEntity tripSeat = tripSeatRepository.findByTripSeatId(request.getTripSeatId());

        if (!"AVAILABLE".equals(tripSeat.getStatus())) {
            throw new RuntimeException("Ghế đã được giữ hoặc đã bán");
        }

        PassengerEntity passenger = null;
        if (request.getPassengerId() != null) {
            passenger = passengerRepository.findById(request.getPassengerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        }

        TicketEntity ticket = new TicketEntity();
        String ticketCode;
        do {
            ticketCode = generateTripCode();
        } while (ticketRepository.existsByTicketCode(ticketCode));
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
        ticketRepository.save(ticket);

        if ("PAY_NOW".equals(request.getPaymentType())) {
            // Ticket
            ticket.setStatus("PAID");
            // Seat
            tripSeat.setStatus("SOLD");
            // Payment
            PaymentEntity payment = new PaymentEntity();
            payment.setTicket(ticket);
            payment.setAmount(ticket.getPrice());
            payment.setMethod(request.getPaymentMethod());
            payment.setStatus("SUCCESS");
            payment.setPaidAt(LocalDateTime.now());
            payment.setCreatedBy(info.getUsername());
            payment.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
        } else { // PAY_LATER
            ticket.setStatus("UNPAID");
            tripSeat.setStatus("HOLD");
        }
        ticketRepository.save(ticket);
        tripSeatRepository.save(tripSeat);
        return new BaseResponse(200, ticket, "Create ticket successfully", null, null);
    }

}
