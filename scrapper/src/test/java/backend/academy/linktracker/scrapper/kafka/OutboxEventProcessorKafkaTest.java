package backend.academy.linktracker.scrapper.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.dto.kafka.LinkUpdateAvroMessage;
import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.mapper.OutboxEventMapper;
import backend.academy.linktracker.scrapper.model.EventStatus;
import backend.academy.linktracker.scrapper.model.EventType;
import backend.academy.linktracker.scrapper.model.OutboxEvent;
import backend.academy.linktracker.scrapper.model.value.EventId;
import backend.academy.linktracker.scrapper.service.crud.OutboxEventService;
import backend.academy.linktracker.scrapper.sheduler.processing.impl.OutboxEventProcessor;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class OutboxEventProcessorKafkaTest {

    static final Network NETWORK = Network.newNetwork();

    @Container
    static final KafkaContainer KAFKA =
        new KafkaContainer(DockerImageName.parse("apache/kafka-native:4.0.0")).withNetwork(NETWORK);

    @Container
    static final GenericContainer<?> SCHEMA_REGISTRY = new GenericContainer<>(
        DockerImageName.parse("confluentinc/cp-schema-registry:7.6.0"))
        .withNetwork(NETWORK)
        .withExposedPorts(8081)
        .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
        .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
        .withEnv(
            "SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS",
            "PLAINTEXT://" + KAFKA.getNetworkAliases().get(0) + ":9092")
        .dependsOn(KAFKA);

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add(
            "spring.kafka.producer.properties.schema.registry.url",
            () -> "http://localhost:" + SCHEMA_REGISTRY.getMappedPort(8081));
        registry.add("app.kafka.topic-link-update", () -> "linkUpdateEvent-outbox-test");
        registry.add("app.kafka.dlt-suffix", () -> "-dlt");
        registry.add("app.kafka.partitions", () -> "1");
        registry.add("app.kafka.replicas", () -> "1");
        registry.add("app.message-sending-type", () -> "KAFKA");
    }

    @Autowired
    private OutboxEventProcessor outboxEventProcessor;

    @Autowired
    private OutboxEventMapper outboxEventMapper;

    @Autowired
    private OutboxEventService outboxEventService;

    private KafkaConsumer<Long, LinkUpdateAvroMessage> testConsumer;

    @BeforeEach
    void setUp() {
        String schemaRegistryUrl = "http://localhost:" + SCHEMA_REGISTRY.getMappedPort(8081);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-outbox-group-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        testConsumer = new KafkaConsumer<>(props);
        testConsumer.subscribe(Collections.singletonList("linkUpdateEvent-outbox-test"));
    }

    @AfterEach
    void tearDown() {
        if (testConsumer != null) {
            testConsumer.close();
        }
    }

    private OutboxEvent buildOutboxEvent(long eventId, long chatId, long linkId, String url) {
        LinkUpdateMessage msg = new LinkUpdateMessage(
            eventId, chatId, linkId, "Title", "user", "preview", url, OffsetDateTime.now());
        OffsetDateTime now = OffsetDateTime.now();
        OutboxEvent event = outboxEventMapper.toOutboxEvent(msg, now);
        // simulate already-persisted event with an ID
        return OutboxEvent.builder()
            .eventId(new EventId(eventId))
            .payload(event.getPayload())
            .status(EventStatus.IN_PROGRESS)
            .createdAt(now)
            .retryTime(now)
            .retryCount(0)
            .type(EventType.LINK_UPDATE_EVENT_TYPE)
            .build();
    }

    @Test
    void shouldProcessOutboxEventsAndSendToKafka() {
        OutboxEvent event = buildOutboxEvent(1L, 42L, 10L, "https://github.com/test/repo");

        outboxEventProcessor.process(List.of(event));

        List<ConsumerRecord<Long, LinkUpdateAvroMessage>> received = new CopyOnWriteArrayList<>();

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            testConsumer.poll(Duration.ofMillis(500)).forEach(received::add);
            assertThat(received).hasSize(1);
        });

        LinkUpdateAvroMessage avroMessage = received.getFirst().value();
        assertThat(avroMessage.getChatId()).isEqualTo(42L);
        assertThat(avroMessage.getUrl()).isEqualTo("https://github.com/test/repo");
    }

    @Test
    void shouldMarkSuccessAfterProcessing() {
        OutboxEvent event = buildOutboxEvent(99L, 55L, 5L, "https://stackoverflow.com/q/1");

        outboxEventProcessor.process(List.of(event));

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> verify(outboxEventService, atLeastOnce())
            .markSuccess(anyCollection()));
    }

    @Test
    void shouldProcessBatchOfEvents() {
        List<OutboxEvent> events = List.of(
            buildOutboxEvent(2L, 10L, 1L, "https://github.com/a/b"),
            buildOutboxEvent(3L, 20L, 2L, "https://github.com/c/d"),
            buildOutboxEvent(4L, 30L, 3L, "https://stackoverflow.com/q/123"));

        outboxEventProcessor.process(events);

        List<ConsumerRecord<Long, LinkUpdateAvroMessage>> received = new CopyOnWriteArrayList<>();

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            testConsumer.poll(Duration.ofMillis(500)).forEach(received::add);
            assertThat(received).hasSize(3);
        });

        assertThat(received)
            .extracting(r -> r.value().getChatId())
            .containsExactlyInAnyOrder(10L, 20L, 30L);
    }
}
