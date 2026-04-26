package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.dto.LinkUpdateMessage;
import backend.academy.linktracker.bot.exception.NotRetryableException;
import backend.academy.linktracker.bot.exception.RetryableException;
import backend.academy.linktracker.bot.properties.KafkaProperties;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.SerializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {
    private final KafkaProperties properties;

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<@NonNull Long,@NonNull LinkUpdateMessage> template) {
        DeadLetterPublishingRecoverer recoverer =
            new DeadLetterPublishingRecoverer(template);
        FixedBackOff backOff =
            new FixedBackOff(properties.getRetryInterval(), properties.getRetryMaxAttempts());

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addRetryableExceptions(RetryableException.class);
        handler.addNotRetryableExceptions(
            NotRetryableException.class,
            DeserializationException.class,
            SerializationException.class
        );

        return handler;
    }
}
