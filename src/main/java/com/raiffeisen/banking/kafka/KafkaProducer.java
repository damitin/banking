package com.raiffeisen.banking.kafka;

import com.raiffeisen.banking.model.AccountDTO;
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
    private KafkaTemplate<String, AccountDTO> kafkaTemplate;

    public void send(String topic, AccountDTO accountDTO) {
        LOGGER.info("Trying to send payload='{}' to topic='{}'", accountDTO, topic);
        CompletableFuture<SendResult<String, AccountDTO>> future = kafkaTemplate.send(topic, accountDTO);
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
