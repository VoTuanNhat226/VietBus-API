package com.vtn.dto.event;

import com.vtn.enumdef.MailEventTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event nhẹ được publish lên Kafka khi có yêu cầu gửi mail cho khách.
 * Chỉ mang id + loại mail; consumer sẽ tự fetch lại TicketEntity mới nhất
 * từ DB để build nội dung, tránh phải serialize entity/Hibernate proxy qua Kafka.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketMailEvent implements Serializable {
    private UUID ticketId;
    private MailEventTypeEnum type;
    private LocalDateTime occurredAt;
}
