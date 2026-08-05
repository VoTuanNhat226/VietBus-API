package com.vtn.producer;

import com.vtn.constant.Constants;
import com.vtn.dto.event.TicketMailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailEventProducer {

    private final KafkaTemplate<String, TicketMailEvent> kafkaTemplate;

    /**
     * Publish event yêu cầu gửi mail cho khách.
     * Key = ticketId để đảm bảo các event của cùng 1 vé được xử lý đúng thứ tự
     * (cùng rơi vào 1 partition).
     */
    public void sendMailEvent(TicketMailEvent event) {
        String key = event.getTicketId().toString();

        kafkaTemplate.send(Constants.TOPIC_TICKET_MAIL_EVENTS, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] Publish mail event failed, ticketId={}, type={}",
                                event.getTicketId(), event.getType(), ex);
                    } else {
                        log.info("[Kafka] Published mail event ticketId={}, type={}, partition={}, offset={}",
                                event.getTicketId(), event.getType(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
