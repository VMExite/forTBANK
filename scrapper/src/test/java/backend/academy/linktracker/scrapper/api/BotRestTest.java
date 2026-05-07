package backend.academy.linktracker.scrapper.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.service.sender.impl.HttpMessageSender;
import backend.academy.linktracker.scrapper.webclient.bot.BotClient;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@WireMockTest(httpPort = 0)
@ActiveProfiles("test")
class BotRestTest {

    private HttpMessageSender sender;

    private static final LinkUpdateMessage REQUEST =
            new LinkUpdateMessage(1L, 1L, 1L, "test", "user", "test preview", "http:example.com", OffsetDateTime.now());

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("app.message-sending-type", () -> "REST");
        registry.add(
                "spring.autoconfigure.exclude",
                () -> "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration");
    }

    @BeforeEach
    void setUp(WireMockRuntimeInfo wm) {

        String baseUrl = wm.getHttpBaseUrl();

        BotClient client = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(
                        RestClient.builder().baseUrl(baseUrl).build()))
                .build()
                .createClient(BotClient.class);

        sender = new HttpMessageSender(client);
    }

    @Test
    void testGreenSendUpdates() {
        WireMock.stubFor(post("/updates").willReturn(aResponse().withStatus(HttpStatus.OK.value())));

        sender.sendMessage(REQUEST);

        WireMock.verify(postRequestedFor(urlEqualTo("/updates")));
    }

    @Test
    void testFailSendUpdates() {
        WireMock.stubFor(post("/updates").willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        assertThrows(RestClientException.class, () -> sender.sendMessage(REQUEST));
    }
}
