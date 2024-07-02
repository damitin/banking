package com.raiffeisen.banking.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    NewTopic bankingTopic() {
        return TopicBuilder
                .name("open-account-topic")
                .partitions(1)
                .replicas(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }
}
//TODO Пока пришлось на локальной машине (MacOS) в /etc/hosts прописать
// 127.0.0.1 kafka-server
// Почему-то проброса порта недостаточно. Ошибка "nodename nor servname provided, or not known".