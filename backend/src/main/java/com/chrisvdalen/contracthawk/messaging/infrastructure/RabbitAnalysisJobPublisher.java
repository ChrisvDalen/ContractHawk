package com.chrisvdalen.contracthawk.messaging.infrastructure;

import com.chrisvdalen.contracthawk.messaging.application.AnalysisJob;
import com.chrisvdalen.contracthawk.messaging.application.AnalysisJobPublisher;
import com.chrisvdalen.contracthawk.messaging.config.MessagingProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitAnalysisJobPublisher implements AnalysisJobPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties properties;

    public RabbitAnalysisJobPublisher(RabbitTemplate rabbitTemplate, MessagingProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(AnalysisJob job) {
        rabbitTemplate.convertAndSend(properties.exchange(), properties.routingKey(), job);
    }
}
