package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.properties.ScrapperProperties;
import backend.academy.linktracker.bot.webclient.ScrapperClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class WebClientsConfig {
    private final ScrapperProperties scrapperProperties;

    @Bean
    ScrapperClient scrapperClient(RestClient.Builder builder) {
        RestClient restClient = builder
            .baseUrl(scrapperProperties.getBaseUrl())
            .build();
        HttpServiceProxyFactory factory =
            HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(ScrapperClient.class);
    }
}
