package backend.academy.linktracker.scrapper.service.sender.impl;

import backend.academy.linktracker.dto.kafka.LinkUpdateAvroMessage;
import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.mapper.AvroMapper;
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
    private final KafkaTemplate<@NonNull Long, @NonNull LinkUpdateAvroMessage> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final AvroMapper avroMapper;

    @Override
    public void sendMessage(LinkUpdateMessage message) {
        LinkUpdateAvroMessage avroMessage = avroMapper.toAvro(message);

        log.info("KAFKA PRODUCER send avro message: {}", avroMessage);
        kafkaTemplate
            .send(kafkaProperties.getTopicLinkUpdate(), avroMessage.getChatId(), avroMessage);
    }
}
