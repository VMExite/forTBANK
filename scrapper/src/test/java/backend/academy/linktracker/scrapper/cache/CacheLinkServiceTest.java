package backend.academy.linktracker.scrapper.cache;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.service.crud.impl.LinksServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class CacheLinkServiceTest {
    private static final int STD_REDIS_PORT = 6379;

    @Container
    static GenericContainer<?> valkey =
            new GenericContainer<>(DockerImageName.parse("valkey/valkey:latest")).withExposedPorts(STD_REDIS_PORT);

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @MockitoSpyBean
    private ChatRepository chatRepository;

    @Autowired
    private LinksServiceImpl linksService;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", valkey::getHost);

        registry.add("spring.data.redis.port", () -> valkey.getMappedPort(STD_REDIS_PORT));

        registry.add("app.message-sending-type", () -> "REST");

        registry.add(
                "spring.autoconfigure.exclude",
                () -> "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration");
    }

    private static final Long CHAT_ID = 1L;

    @BeforeEach
    void init() {
        when(chatRepository.findById(new ChatId(CHAT_ID)))
                .thenReturn(
                        Optional.of(Chat.builder().chatId(new ChatId(CHAT_ID)).build()));
    }

    @Test
    void testGetLinksCached() {
        linksService.getLinks(CHAT_ID);
        linksService.getLinks(CHAT_ID);

        verify(chatRepository, times(1)).findById(any());
    }
}
