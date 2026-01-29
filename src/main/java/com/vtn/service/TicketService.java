package com.vtn.service;

import com.vtn.dto.request.TicketRequest;
import com.vtn.dto.response.TicketResponse;
import com.vtn.entity.*;
import com.vtn.repository.*;
import com.vtn.utils.BaseResponse;
import com.vtn.utils.CodeGeneratorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final TripSeatRepository tripSeatRepository;
    private final PassengerRepository passengerRepository;
    private final PaymentRepository paymentRepository;

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

    private boolean isAllParametersNull(TicketRequest request) {
        return ((request.getTicketCode() == null) &&
                (request.getTripCode() == null) &&
                (request.getTicketPaymentType() == null) &&
                (request.getTicketSoldBy() == null));
    }

    public BaseResponse getAllTicketsUnpaid(TicketRequest request) {
        try {
            List<TicketResponse> tickets;
            if(isAllParametersNull(request)) {
                tickets = ticketRepository.findAllTicketUnPaid()
                        .stream()
                        .map(t -> new TicketResponse(
                                t.getTicketId(),
                                t.getTicketCode(),
                                t.getPrice(),
                                t.getStatus(),
                                t.getPaymentType(),
                                t.getSoldBy(),
                                t.getTrip().getTripCode(),
                                t.getTrip().getRoute().getFromStation().getName(),
                                t.getTrip().getRoute().getToStation().getName(),
                                t.getTripSeat().getSeat().getSeatNumber()
                        ))
                        .toList();
            } else {
                tickets = ticketRepository.getAllByCondition(
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
                                t.getTrip().getTripCode(),
                                t.getTrip().getRoute().getFromStation().getName(),
                                t.getTrip().getRoute().getToStation().getName(),
                                t.getTripSeat().getSeat().getSeatNumber()
                        ))
                        .toList();
            }
            return new BaseResponse(200,tickets,"Get all tickets unpaid successfully", null,null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public BaseResponse createTicket(TicketRequest request) {

        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

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
            ticketCode = CodeGeneratorUtil.generateCode();
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
        return new BaseResponse(201, ticket, "Create ticket successfully", null, null);
    }

    @Transactional
    public BaseResponse updateTicket(TicketRequest request) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TicketEntity ticket = ticketRepository.findByTicketCode(request.getTicketCode());
        if (ticket == null) {
            return new BaseResponse(404,null,"Không tìm thấy vé xe",null,null);
        }

        TripEntity trip = tripRepository.findByTripCode(request.getTripCode());
        if (trip == null) {
            return new BaseResponse(404,null,"Không tìm thấy chuyến xe",null,null);
        }

        String currentTicketStatus = ticket.getStatus();
        if ("PAID".equals(currentTicketStatus)) {
            return new BaseResponse(400, null, "Vé đã được thanh toán", null, null);
        }

        if("UNPAID".equals(currentTicketStatus) && "PAID".equals(request.getTicketStatus())) {
            ticket.setStatus(request.getTicketStatus());
            ticketRepository.save(ticket);

            TripSeatEntity tripSeat = ticket.getTripSeat();
            tripSeat.setStatus("SOLD");
            tripSeatRepository.save(tripSeat);

            PaymentEntity payment = new PaymentEntity();
            payment.setTicket(ticket);
            payment.setAmount(ticket.getPrice());
            payment.setMethod(request.getPaymentMethod());
            payment.setStatus("SUCCESS");
            payment.setPaidAt(LocalDateTime.now());
            payment.setCreatedBy(info.getUsername());
            payment.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            return new BaseResponse(200, null, "Update ticket successfully, created payment", null, null);
        }
        return new BaseResponse(200, null, "Update ticket successfully", null, null);
    }
}
