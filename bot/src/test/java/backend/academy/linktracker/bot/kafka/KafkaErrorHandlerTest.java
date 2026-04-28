package backend.academy.linktracker.bot.kafka;

import backend.academy.linktracker.bot.configuration.KafkaConfiguration;
import backend.academy.linktracker.bot.consumer.UpdateKafkaConsumer;
import backend.academy.linktracker.bot.dto.LinkUpdateMessage;
import backend.academy.linktracker.bot.exception.RetryableException;
import backend.academy.linktracker.bot.properties.KafkaProperties;
import backend.academy.linktracker.bot.service.UpdateNotificationService;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import java.time.Duration;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(
    classes = {
        KafkaConfiguration.class,
        UpdateKafkaConsumer.class
    }
)
@Testcontainers
@ActiveProfiles("test")
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaErrorHandlerTest {

    @Autowired
    private KafkaTemplate<Long, LinkUpdateMessage> kafkaTemplate;

    @MockitoBean
    private UpdateNotificationService notificationService;

    @Container
    static KafkaContainer kafkaContainer =
        new KafkaContainer("apache/kafka:3.9.1");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers",
            kafkaContainer::getBootstrapServers);
        registry.add("app.kafka.retry-interval", () -> "1000");
        registry.add("app.kafka.retry-max-attempts", () -> "3");
    }

    private static LinkUpdateMessage testMessage;

    @BeforeAll
    static void setUp() {
        testMessage = new LinkUpdateMessage(
            1L, 1L,
            "test", "test", "test",
            "test", OffsetDateTime.now()
        );
    }

    @Test
    void retryAndToDltSendTest() {

        doThrow(new RetryableException("test"))
            .when(notificationService)
            .notifyUsers(any());

        kafkaTemplate.send("linkUpdateEvent", 1L, testMessage);

        await()
            .atMost(Duration.ofSeconds(20))
            .untilAsserted(() ->
                verify(notificationService, atLeast(3))
                    .notifyUsers(any())
            );
    }
}
