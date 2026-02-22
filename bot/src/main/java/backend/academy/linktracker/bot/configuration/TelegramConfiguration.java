package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.properties.MessageSourceProperties;
import backend.academy.linktracker.bot.properties.TelegramProperties;
import com.pengrad.telegrambot.TelegramBot;
import java.nio.charset.StandardCharsets;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class TelegramConfiguration {

    @Bean
    protected TelegramBot telegramBot(TelegramProperties properties) {
        TelegramBot.Builder builder = new TelegramBot.Builder(properties.getToken())
                .apiUrl(properties.getUrl())
                .updateListenerSleep(properties.getUpdateListenerSleep().toMillis());
        if (properties.isDebug()) {
            builder.debug();
        }
        return builder.build();
    }

    @Bean
    protected MessageSource messageSource(MessageSourceProperties properties) {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename(properties.getBasename());
        source.setDefaultEncoding(StandardCharsets.UTF_8.name());
        return source;
    }
}
