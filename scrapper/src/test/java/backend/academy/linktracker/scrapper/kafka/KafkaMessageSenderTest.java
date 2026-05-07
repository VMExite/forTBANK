package backend.academy.linktracker.scrapper.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import backend.academy.linktracker.dto.kafka.LinkUpdateAvroMessage;
import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.service.sender.impl.KafkaMessageSender;
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
class KafkaMessageSenderTest {

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
        registry.add("app.kafka.topic-link-update", () -> "linkUpdateEvent-test");
        registry.add("app.kafka.dlt-suffix", () -> "-dlt");
        registry.add("app.kafka.partitions", () -> "1");
        registry.add("app.kafka.replicas", () -> "1");
        registry.add("app.message-sending-type", () -> "KAFKA");
    }

    @Autowired
    private KafkaMessageSender kafkaMessageSender;

    private KafkaConsumer<Long, LinkUpdateAvroMessage> testConsumer;

    @BeforeEach
    void setUp() {
        String schemaRegistryUrl = "http://localhost:" + SCHEMA_REGISTRY.getMappedPort(8081);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        testConsumer = new KafkaConsumer<>(props);
        testConsumer.subscribe(Collections.singletonList("linkUpdateEvent-test"));
    }

    @AfterEach
    void tearDown() {
        if (testConsumer != null) {
            testConsumer.close();
        }
    }

    @Test
    void shouldSendMessageToKafka() {
        LinkUpdateMessage message = new LinkUpdateMessage(
            1L,
            42L,
            10L,
            "Test Title",
            "test_user",
            "Test preview text",
            "https://github.com/test/repo",
            OffsetDateTime.now());

        kafkaMessageSender.sendMessage(message);

        List<ConsumerRecord<Long, LinkUpdateAvroMessage>> received = new CopyOnWriteArrayList<>();

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            testConsumer.poll(Duration.ofMillis(500)).forEach(received::add);
            assertThat(received).hasSize(1);
        });

        ConsumerRecord<Long, LinkUpdateAvroMessage> record = received.getFirst();
        assertThat(record.key()).isEqualTo(42L);

        LinkUpdateAvroMessage avroMessage = record.value();
        assertThat(avroMessage.getChatId()).isEqualTo(42L);
        assertThat(avroMessage.getLinkId()).isEqualTo(10L);
        assertThat(avroMessage.getTitle()).isEqualTo("Test Title");
        assertThat(avroMessage.getUsername()).isEqualTo("test_user");
        assertThat(avroMessage.getPreview()).isEqualTo("Test preview text");
        assertThat(avroMessage.getUrl()).isEqualTo("https://github.com/test/repo");
    }

    @Test
    void shouldSendMultipleMessagesToKafka() {
        LinkUpdateMessage message1 = new LinkUpdateMessage(
            1L, 10L, 1L, "Title 1", "user1", "preview1", "https://github.com/a/b", OffsetDateTime.now());
        LinkUpdateMessage message2 = new LinkUpdateMessage(
            2L, 20L, 2L, "Title 2", "user2", "preview2", "https://github.com/c/d", OffsetDateTime.now());

        kafkaMessageSender.sendMessage(message1);
        kafkaMessageSender.sendMessage(message2);

        List<ConsumerRecord<Long, LinkUpdateAvroMessage>> received = new CopyOnWriteArrayList<>();

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            testConsumer.poll(Duration.ofMillis(500)).forEach(received::add);
            assertThat(received).hasSize(2);
        });

        assertThat(received)
            .extracting(r -> r.value().getChatId())
            .containsExactlyInAnyOrder(10L, 20L);
    }
}
