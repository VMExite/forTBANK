package backend.academy.linktracker.scrapper.service.sender.impl;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.properties.KafkaProperties;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-sending-type", havingValue = "KAFKA", matchIfMissing = true)
// kafka producer
public class KafkaMessageSender implements MessageSender {
    private final KafkaTemplate<@NonNull Long, @NonNull LinkUpdateMessage> kafkaTemplate;
    private final KafkaProperties  kafkaProperties;

    @Override
    public void sendMessage(LinkUpdateMessage message) {
        log.warn("KAFKA message sending is don't implemented yet");
        kafkaTemplate.send(kafkaProperties.getTopicLinkUpdate(), message.chatId(), message);
    }
}
