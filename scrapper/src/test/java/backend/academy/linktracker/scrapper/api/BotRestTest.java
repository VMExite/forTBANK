package backend.academy.linktracker.scrapper.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.linktracker.scrapper.ScrapperApplication;
import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.service.sender.impl.HttpMessageSender;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClientException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ScrapperApplication.class)
class BotRestTest {

    @Autowired
    private HttpMessageSender sender;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("app.bot.base-url", wireMock::baseUrl);
    }

    @Test
    void testGreenSendUpdates() {
        wireMock.stubFor(post(urlEqualTo("/updates")).willReturn(aResponse().withStatus(HttpStatus.OK.value())));

        LinkUpdateMessage request = new LinkUpdateMessage(
                1L,
                100L,
                "New issue",
                "vexi",
                "short preview",
                "https://github.com/test/repo/issues/1",
                OffsetDateTime.now());

        assertDoesNotThrow(() -> sender.sendMessage(request));

        wireMock.verify(postRequestedFor(urlEqualTo("/updates")));
    }

    @Test
    void testFailSendUpdates() {
        wireMock.stubFor(post(urlEqualTo("/updates"))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        LinkUpdateMessage request = new LinkUpdateMessage(
                1L,
                100L,
                "New issue",
                "vexi",
                "short preview",
                "https://github.com/test/repo/issues/1",
                OffsetDateTime.now());

        assertThrows(RestClientException.class, () -> sender.sendMessage(request));
    }
}
