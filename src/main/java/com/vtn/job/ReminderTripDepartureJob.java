package com.vtn.job;

import com.vtn.entity.TicketEntity;
import com.vtn.entity.TripSeatEntity;
import com.vtn.enumdef.TicketStatusEnum;
import com.vtn.enumdef.TripSeatStatusEnum;
import com.vtn.repository.TicketRepository;
import com.vtn.service.Mail.MailService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReminderTripDepartureJob implements Job {
    private final TicketRepository ticketRepository;
    private final MailService mailService;

    @Override
    public void execute(JobExecutionContext context) {

        String tripId = context.getMergedJobDataMap().getString("tripId");
        List<TicketEntity> tickets = ticketRepository.getAllByTripId(UUID.fromString(tripId));
        for (TicketEntity ticket : tickets) {
            if(ticket.getPassenger() != null && ticket.getPassenger().getEmail() != null) {
                byte[] qr = mailService.generateQrAsBytes(ticket.getTicketCode());
                mailService.sendHtmlMail(
                        ticket.getPassenger().getEmail(),
                        "NHẮC NHỞ CHUYẾN ĐI CỦA BẠN",
                        mailService.buildTripReminderMailContent(ticket),
                        qr,
                        "ticketQr"
                );
            }
        }

        System.out.println("Reminder passenger before departure for tripId: " + tripId);
    }
}
