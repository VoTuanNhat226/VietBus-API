package com.vtn.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Cấu hình xử lý lỗi cho toàn bộ @KafkaListener trong app:
 * - Retry tối đa 3 lần, cách nhau 2s (ví dụ: SMTP tạm thời lỗi).
 * - Hết retry mà vẫn lỗi -> đẩy message sang topic "<topic>.DLT" để không mất event
 *   và không làm kẹt consumer group ở message lỗi.
 */
@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 3L));

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("[Kafka] Retry #{} for record key={} on topic={} failed: {}", deliveryAttempt, record.key(), record.topic(), ex.getMessage())
        );

        return errorHandler;
    }
}
