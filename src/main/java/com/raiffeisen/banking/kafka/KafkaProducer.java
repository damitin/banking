package com.raiffeisen.banking.kafka;

import com.raiffeisen.banking.kafka.event.KafkaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class KafkaProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);

    @Autowired
    private KafkaTemplate<String, KafkaEvent> kafkaTemplate;

    public void send(String topic, KafkaEvent kafkaEvent) {
        LOGGER.info("Trying to send payload='{}' to topic='{}'", kafkaEvent, topic);
        CompletableFuture<SendResult<String, KafkaEvent>> future = kafkaTemplate.send(topic, kafkaEvent);
        future.whenComplete((result, exception) -> {
            if (exception != null) {
                logFailure(exception);
            } else {
                logSuccess(result);
            }
        });
    }

    private void logSuccess(SendResult result) {
        LOGGER.info("Message sent to Kafka {}", result.getRecordMetadata());
    }

    private void logFailure(Throwable throwable) {
        LOGGER.error(throwable.getMessage(), throwable);
    }
}
