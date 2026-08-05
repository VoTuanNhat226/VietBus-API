package com.vtn.job;

import com.vtn.dto.event.TicketMailEvent;
import com.vtn.entity.TicketEntity;
import com.vtn.enumdef.MailEventTypeEnum;
import com.vtn.producer.MailEventProducer;
import com.vtn.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderTripDepartureJob implements Job {
    private final TicketRepository ticketRepository;
    private final MailEventProducer mailEventProducer;

    @Override
    public void execute(JobExecutionContext context) {

        String tripId = context.getMergedJobDataMap().getString("tripId");
        List<TicketEntity> tickets = ticketRepository.getAllByTripId(UUID.fromString(tripId));

        for (TicketEntity ticket : tickets) {
            if (ticket.getPassenger() != null && ticket.getPassenger().getEmail() != null) {
                mailEventProducer.sendMailEvent(
                        new TicketMailEvent(ticket.getTicketId(), MailEventTypeEnum.TRIP_REMINDER, LocalDateTime.now())
                );
            }
        }

        log.info("Published reminder mail events before departure for tripId: {}", tripId);
    }
}
