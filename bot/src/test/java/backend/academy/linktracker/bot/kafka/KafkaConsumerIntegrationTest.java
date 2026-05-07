package backend.academy.linktracker.bot.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.repository.LinkUpdateRepository;
import backend.academy.linktracker.bot.service.UpdateNotificationService;
import backend.academy.linktracker.dto.kafka.LinkUpdateAvroMessage;
import com.pengrad.telegrambot.TelegramBot;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class UpdateKafkaConsumerIntegrationTest {

    static final Network NETWORK = Network.newNetwork();

    @Container
    static final KafkaContainer KAFKA =
        new KafkaContainer(DockerImageName.parse("apache/kafka-native:4.0.0"))
            .withNetwork(NETWORK)
            .withNetworkAliases("kafka")
            .withExposedPorts(9092)
            .withEnv("KAFKA_NODE_ID", "1")
            .withEnv("KAFKA_PROCESS_ROLES", "broker,controller")
            .withEnv("KAFKA_LISTENERS",
                "PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093")
            .withEnv("KAFKA_ADVERTISED_LISTENERS",
                "PLAINTEXT://kafka:9092")
            .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP",
                "PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT")
            .withEnv("KAFKA_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
            .withEnv("KAFKA_CONTROLLER_QUORUM_VOTERS", "1@kafka:9093")
            .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
            .withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "0")
            .withEnv("CLUSTER_ID", "MkU3OEVBNTcwNTJENDM2Qk")
            .waitingFor(Wait.forListeningPort());

    @Container
    static final GenericContainer<?> SCHEMA_REGISTRY =
        new GenericContainer<>(DockerImageName.parse("confluentinc/cp-schema-registry:7.6.0"))
            .withNetwork(NETWORK)
            .withNetworkAliases("schema-registry")
            .withExposedPorts(8081)
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
            .withEnv(
                "SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS",
                "PLAINTEXT://kafka:9092")
            .dependsOn(KAFKA)
            .waitingFor(
                Wait.forHttp("/subjects")
                    .forPort(8081)
                    .forStatusCode(200)
            );

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.kafka.bootstrap-servers",
            () -> "localhost:" + KAFKA.getMappedPort(9092));

        registry.add(
            "spring.kafka.consumer.properties.schema.registry.url",
            () -> "http://localhost:" + SCHEMA_REGISTRY.getMappedPort(8081));

        registry.add(
            "spring.kafka.producer.properties.schema.registry.url",
            () -> "http://localhost:" + SCHEMA_REGISTRY.getMappedPort(8081));

        registry.add("spring.liquibase.change-log", () -> "migrations/master.yaml");

        registry.add("app.kafka.link-update-topic", () -> "linkUpdateEvent");
        registry.add("app.kafka.link-update-topic-dlt", () -> "linkUpdateEvent-dlt");
        registry.add("app.kafka.retry-interval", () -> "100");
        registry.add("app.kafka.retry-max-attempts", () -> "1");
    }

    @MockitoBean
    private TelegramBot telegramBot;

    @MockitoBean
    private UpdateNotificationService updateNotificationService;

    @MockitoBean
    private LinkUpdateRepository linkUpdateRepository;

    private KafkaProducer<Long, LinkUpdateAvroMessage> testProducer;

    @BeforeEach
    void setUp() {
        String schemaRegistryUrl =
            "http://localhost:" + SCHEMA_REGISTRY.getMappedPort(18081);

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put("schema.registry.url", schemaRegistryUrl);

        testProducer = new KafkaProducer<>(props);

        doNothing().when(updateNotificationService).notifyUsers(any());
    }

    @AfterEach
    void tearDown() {
        if (testProducer != null) {
            testProducer.close();
        }
    }

    private LinkUpdateAvroMessage buildAvroMessage(long eventId, long chatId) {
        return LinkUpdateAvroMessage.newBuilder()
            .setEventId(eventId)
            .setChatId(chatId)
            .setLinkId(1L)
            .setTitle("Test Title")
            .setUsername("test_user")
            .setPreview("Test preview")
            .setUrl("https://github.com/test/repo")
            .setCreatedAt(Instant.now())
            .build();
    }

    @Test
    void shouldConsumeMessageAndNotifyUser() {
        long eventId = 100L;
        long chatId = 42L;

        when(linkUpdateRepository.existsByEventId(eventId)).thenReturn(false);

        testProducer.send(new ProducerRecord<>("linkUpdateEvent", chatId, buildAvroMessage(eventId, chatId)));
        testProducer.flush();

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
            verify(updateNotificationService, atLeastOnce()).notifyUsers(any()));

        ArgumentCaptor<backend.academy.linktracker.bot.dto.LinkUpdateMessage> captor =
            ArgumentCaptor.forClass(backend.academy.linktracker.bot.dto.LinkUpdateMessage.class);
        verify(updateNotificationService).notifyUsers(captor.capture());

        assertThat(captor.getValue().chatId()).isEqualTo(chatId);
        assertThat(captor.getValue().title()).isEqualTo("Test Title");
    }

    @Test
    void shouldSkipAlreadyProcessedMessage() {
        long eventId = 200L;
        long chatId = 55L;

        when(linkUpdateRepository.existsByEventId(eventId)).thenReturn(true);

        testProducer.send(new ProducerRecord<>("linkUpdateEvent", chatId, buildAvroMessage(eventId, chatId)));
        testProducer.flush();

        // Даём потребителю время сработать, затем убеждаемся что notify не вызван
        await().atMost(Duration.ofSeconds(15))
            .pollDelay(Duration.ofSeconds(3))
            .untilAsserted(() -> verify(updateNotificationService, never()).notifyUsers(any()));
    }

    @Test
    void shouldSaveEventIdAfterSuccessfulProcessing() {
        long eventId = 300L;
        long chatId = 77L;

        when(linkUpdateRepository.existsByEventId(eventId)).thenReturn(false);

        testProducer.send(new ProducerRecord<>("linkUpdateEvent", chatId, buildAvroMessage(eventId, chatId)));
        testProducer.flush();

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
            verify(linkUpdateRepository, atLeastOnce())
                .save(any(backend.academy.linktracker.bot.model.LinkUpdate.class)));

        ArgumentCaptor<backend.academy.linktracker.bot.model.LinkUpdate> captor =
            ArgumentCaptor.forClass(backend.academy.linktracker.bot.model.LinkUpdate.class);
        verify(linkUpdateRepository).save(captor.capture());
        assertThat(captor.getValue().eventId()).isEqualTo(eventId);
    }
}
