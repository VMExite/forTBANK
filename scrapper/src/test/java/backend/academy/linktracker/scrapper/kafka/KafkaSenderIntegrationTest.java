package backend.academy.linktracker.scrapper.kafka;

import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.properties.KafkaProperties;
import backend.academy.linktracker.scrapper.service.sender.impl.KafkaMessageSender;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@RequiredArgsConstructor
public class KafkaSenderIntegrationTest {
    private final KafkaMessageSender sender;
    private final KafkaProperties kafkaProperties;
    private static Consumer<Long, LinkUpdateMessage> consumer;

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer("apache/kafka:3.9.1");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @BeforeAll
    static void setUp() {
        kafkaContainer.start();
    }

    @AfterAll
    static void tearDown() {
        kafkaContainer.stop();
    }

    @BeforeEach
    void init() {
        consumer.subscribe(Collections.singleton(kafkaProperties.getTopicLinkUpdate()));
    }

    @Test
    public void greenWayTest() {
        LinkUpdateMessage message = new LinkUpdateMessage(
                1L,
                1L,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                OffsetDateTime.now());
        sender.sendMessage(message);

        ConsumerRecords<Long, LinkUpdateMessage> records = consumer.poll(Duration.ofSeconds(5));
        boolean found = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(records.iterator(), Spliterator.ORDERED),
                        false) // consumer returns iterator need wrap it to stream
                .anyMatch(r -> r.value().equals(message));

        assertTrue(found);
    }
}
