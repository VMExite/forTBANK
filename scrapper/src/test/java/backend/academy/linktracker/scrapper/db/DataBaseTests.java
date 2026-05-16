package backend.academy.linktracker.scrapper.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.scrapper.exception.LinkAlreadyTracked;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import backend.academy.linktracker.scrapper.model.value.LinkId;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Slf4j
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "classpath:/sql/clear.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class DataBaseTests {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")
            .withUrlParam("ssl", "false");

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);

        registry.add("app.message-sending-type", () -> "REST");
        registry.add(
                "spring.autoconfigure.exclude",
                () -> "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration");
    }

    private Chat chatWithoutLink;
    private Chat chatWithLink;
    private Link link;

    @BeforeEach
    public void setup() {
        chatWithoutLink =
                Chat.builder().chatId(new ChatId(1L)).links(new HashSet<>()).build();
        link = Link.builder()
                .linkId(new LinkId(1L))
                .url("https://edu.tbank.ru/")
                .tags(new HashSet<>())
                .build();
        chatWithLink = Chat.builder()
                .chatId(new ChatId(1L))
                .links(new HashSet<>(Set.of(link)))
                .build();
    }

    @Test
    public void testGreenAddLink() {
        chatWithoutLink.addLink(link);

        Chat result = chatRepository().save(chatWithoutLink);

        assertFalse(result.getLinks().isEmpty());
    }

    @Test
    public void testGreenDeleteLink() {
        chatRepository().save(chatWithLink);

        chatWithLink.removeLink(link);
        Chat result = chatRepository().save(chatWithLink);

        assertTrue(result.getLinks().isEmpty());
        assertEquals(0, result.getLinks().size());
    }

    @Test
    public void testThrowAddDubleLink() {
        chatRepository().save(chatWithLink);

        assertThrows(LinkAlreadyTracked.class, () -> chatWithLink.addLink(link));
    }

    protected abstract ChatRepository chatRepository();
}
