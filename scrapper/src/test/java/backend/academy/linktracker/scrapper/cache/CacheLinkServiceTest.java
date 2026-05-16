package backend.academy.linktracker.scrapper.cache;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.mapper.LinkMapper;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import backend.academy.linktracker.scrapper.model.value.LinkId;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.service.crud.impl.LinksServiceImpl;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

    @MockitoSpyBean
    private LinkMapper mapper;

    @Autowired
    private LinksServiceImpl linksService;

    private static final Long CHAT_ID = 1L;

    private Chat chat;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", valkey::getHost);
        registry.add("spring.data.redis.port", () -> valkey.getMappedPort(STD_REDIS_PORT));
        registry.add("app.message-sending-type", () -> "REST");
        registry.add("app.cache.ttl", () -> "1");
    }

    @BeforeEach
    void setUp() {
        Link link = Link.builder()
                .linkId(new LinkId(1L))
                .url("https://edu.tbank.ru/")
                .tags(new HashSet<>())
                .build();

        chat = Chat.builder()
                .chatId(new ChatId(CHAT_ID))
                .links(new HashSet<>(Set.of(link)))
                .build();

        when(chatRepository.findById(any())).thenReturn(Optional.of(chat));
    }

    @Test
    void testGetLinksCached() {
        linksService.getLinks(chat.getChatId().value());
        linksService.getLinks(chat.getChatId().value());

        verify(chatRepository, times(1)).findById(any(ChatId.class));
    }
}
