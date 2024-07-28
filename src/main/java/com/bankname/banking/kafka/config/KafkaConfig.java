package com.bankname.banking.kafka.config;

import com.bankname.banking.model.AccountDTO;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;

    @Value("${spring.kafka.producer.retries}")
    private String retries;

    @Value("${spring.kafka.producer.properties.retry.backoff.ms}")
    private String retryBackoffMs;

    @Value("${spring.kafka.producer.properties.enable.idempotence}")
    private String enableIdempotence;

    Map<String, Object> producerConfigs() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        config.put(ProducerConfig.RETRIES_CONFIG, retries);
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoffMs);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);

        return config;
    }

    @Bean
    ProducerFactory<String, AccountDTO> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    KafkaTemplate<String, AccountDTO> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    NewTopic openAccountTopic() {
        return TopicBuilder
                .name("open-account-topic")
                .partitions(3)
                .replicas(3)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }

    @Bean
    NewTopic closeAccountTopic() {
        return TopicBuilder
                .name("close-account-topic")
                .partitions(3)
                .replicas(3)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }

    @Bean
    NewTopic depositAccountTopic() {
        return TopicBuilder
                .name("deposit-account-topic")
                .partitions(3)
                .replicas(3)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }

    @Bean
    NewTopic withdrawAccountTopic() {
        return TopicBuilder
                .name("withdraw-account-topic")
                .partitions(3)
                .replicas(3)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }
}