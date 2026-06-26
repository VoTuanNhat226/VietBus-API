package com.vtn.service.Quartz;

import com.vtn.entity.TripEntity;
import com.vtn.job.CancelPayLaterTicketJob;
import com.vtn.job.ReminderTripDepartureJob;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuartzService {
    private final Scheduler scheduler;

    public void scheduleCancelTicket(UUID ticketId) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(CancelPayLaterTicketJob.class)
                    .withIdentity("cancel-ticket-" + ticketId)
                    .usingJobData("ticketId", String.valueOf(ticketId))
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("cancel-trigger-" + ticketId)
                    .startAt(Date.from(
                            Instant.now().plus(1, ChronoUnit.HOURS)
                    ))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public void scheduleReminderTripBeforeDeparture(TripEntity trip) {
        try {
            Instant reminderTime = trip.getDepartureTime()
                    .minusHours(2)
                    .atZone(ZoneId.systemDefault())
                    .toInstant();

            JobDetail jobDetail = JobBuilder.newJob(ReminderTripDepartureJob.class)
                    .withIdentity("reminder-trip-" + trip.getTripId())
                    .usingJobData("tripId", trip.getTripId().toString())
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("reminder-trigger-" + trip.getTripId())
                    .startAt(Date.from(reminderTime))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}
