package com.vtn.config;

import com.vtn.constant.Constants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic ticketMailEventsTopic() {
        return TopicBuilder.name(Constants.TOPIC_TICKET_MAIL_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ticketMailEventsDltTopic() {
        return TopicBuilder.name(Constants.TOPIC_TICKET_MAIL_EVENTS_DLT)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
