package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.dto.LinkUpdateMessage;
import backend.academy.linktracker.bot.exception.NotRetryableException;
import backend.academy.linktracker.bot.exception.RetryableException;
import backend.academy.linktracker.bot.properties.KafkaProperties;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.SerializationException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    private final KafkaProperties properties;

    @Bean
    public KafkaTemplate<Long, LinkUpdateMessage> dltKafkaTemplate(
        ProducerFactory<Long, LinkUpdateMessage> factory
    ) {
        return new KafkaTemplate<>(factory);
    }

    @Bean
    public DeadLetterPublishingRecoverer recoverer(
        KafkaTemplate<Long, LinkUpdateMessage> template
    ) {
        return new DeadLetterPublishingRecoverer(
            template,
            (record, ex) ->
                new TopicPartition(record.topic() + "-dlt", record.partition())
        );
    }

    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {

        DefaultErrorHandler handler =
            new DefaultErrorHandler(recoverer, new FixedBackOff(properties.getRetryInterval(), properties.getRetryMaxAttempts()));

        handler.addNotRetryableExceptions(
            DeserializationException.class,
            SerializationException.class
        );

        return handler;
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, LinkUpdateMessage>
    kafkaListenerContainerFactory(
        ConsumerFactory<Long, LinkUpdateMessage> consumerFactory,
        DefaultErrorHandler errorHandler) {

        ConcurrentKafkaListenerContainerFactory<Long, LinkUpdateMessage> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
