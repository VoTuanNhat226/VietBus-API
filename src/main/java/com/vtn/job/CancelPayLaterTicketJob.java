package com.vtn.job;

import com.vtn.entity.TicketEntity;
import com.vtn.entity.TripSeatEntity;
import com.vtn.enumdef.TicketStatusEnum;
import com.vtn.enumdef.TripSeatStatusEnum;
import com.vtn.repository.TicketRepository;
import com.vtn.repository.TripSeatRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CancelPayLaterTicketJob implements Job {
    private final TicketRepository ticketRepository;
    private final TripSeatRepository tripSeatRepository;

    @Override
    public void execute(JobExecutionContext context) {

        String ticketId = context.getMergedJobDataMap().getString("ticketId");

        TicketEntity ticket = ticketRepository.findByTicketId(UUID.fromString(ticketId));

        if (ticket == null)
            return;

        // Đã thanh toán
        if (ticket.getStatus() == TicketStatusEnum.PAID)
            return;

        // Đã hủy trước đó
        if (ticket.getStatus() == TicketStatusEnum.CANCELED)
            return;

        ticket.setStatus(TicketStatusEnum.CANCELED);
        ticketRepository.save(ticket);

        TripSeatEntity tripSeat = ticket.getTripSeat();
        tripSeat.setStatus(TripSeatStatusEnum.AVAILABLE);
        tripSeatRepository.save(tripSeat);

        System.out.println("Ticket " + ticketId + " cancelled.");
    }
}
