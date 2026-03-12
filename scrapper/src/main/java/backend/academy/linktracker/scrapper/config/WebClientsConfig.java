package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.properties.BotProperties;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import backend.academy.linktracker.scrapper.webclient.bot.BotClient;
import backend.academy.linktracker.scrapper.webclient.github.GitHubClient;
import backend.academy.linktracker.scrapper.webclient.stackoverflow.StackOverflowClient;
import com.google.common.net.HttpHeaders;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class WebClientsConfig {
    private final GithubProperties githubProperties;
    private final StackoverflowProperties stackoverflowProperties;
    private final BotProperties botProperties;

    @Bean
    BotClient botClient(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl(botProperties.getBaseUrl()).build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(BotClient.class);
    }

    @Bean
    GitHubClient gitHubClient(RestClient.Builder builder) {
        RestClient.Builder clientBuilder = builder.baseUrl(githubProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.USER_AGENT, "link-tracker")
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json");
        String token = githubProperties.getToken();

        if (token != null && !token.isBlank()) {
            clientBuilder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        RestClient restClient = clientBuilder.build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(GitHubClient.class);
    }

    @Bean
    StackOverflowClient stackOverflowClient(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl(stackoverflowProperties.getBaseUrl())
                .defaultUriVariables(Map.of("site", "stackoverflow", "key", stackoverflowProperties.getKey()))
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(StackOverflowClient.class);
    }
}
