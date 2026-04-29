package backend.academy.linktracker.bot.consumer;

import backend.academy.linktracker.bot.dto.LinkUpdateMessage;
import backend.academy.linktracker.bot.mapper.AvroMapper;
import backend.academy.linktracker.bot.service.UpdateNotificationService;
import backend.academy.linktracker.dto.kafka.LinkUpdateAvroMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
// kafka consumer
public class UpdateKafkaConsumer {
    private final UpdateNotificationService updateNotificationService;
    private final AvroMapper avroMapper;

    @KafkaListener(topics = "linkUpdateEvent", groupId = "linkUpdateGroup")
    public void consume(ConsumerRecord<Long, LinkUpdateAvroMessage> consumerRecord) {
        log.info("RECEIVED topic={}, key={}, value={} class={}",
            consumerRecord.topic(),
            consumerRecord.key(),
            consumerRecord.value(),
            consumerRecord.value().getClass().getName());
        if (consumerRecord.value() == null) {
            log.error("Deserialization failed");

            return;
        }
        LinkUpdateMessage message = avroMapper.fromAvro(consumerRecord.value());
        log.debug("Consumer consume the message: {}", consumerRecord.value());
        updateNotificationService.notifyUsers(message);
    }
}
