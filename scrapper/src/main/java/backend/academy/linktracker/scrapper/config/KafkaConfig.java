package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.properties.KafkaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    private final KafkaProperties kafkaProperties;

    @Bean
    public NewTopic topicLinkUpdate() {
        String topic = kafkaProperties.getTopicLinkUpdate();
        int partitions = kafkaProperties.getPartitions();
        short replicas = kafkaProperties.getReplicas();

        log.info("TOPIC {} PARTITIONS {} REPLICAS {}", topic, partitions, replicas);
        return TopicBuilder.name(topic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic linkUpdateDlt() {
        String topic = kafkaProperties.getTopicLinkUpdate() + kafkaProperties.getDltSuffix();
        int partitions = kafkaProperties.getPartitions();
        short replicas = kafkaProperties.getReplicas();

        return TopicBuilder.name(topic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
