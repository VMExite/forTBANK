package backend.academy.linktracker.bot.consumer;

import backend.academy.linktracker.bot.dto.LinkUpdateMessage;
import backend.academy.linktracker.bot.exception.NotificationFailedException;
import backend.academy.linktracker.bot.exception.RetryableException;
import backend.academy.linktracker.bot.mapper.AvroMapper;
import backend.academy.linktracker.bot.model.LinkUpdate;
import backend.academy.linktracker.bot.repository.LinkUpdateRepository;
import backend.academy.linktracker.bot.service.UpdateNotificationService;
import backend.academy.linktracker.dto.kafka.LinkUpdateAvroMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
// kafka consumer
public class UpdateKafkaConsumer {
    private final LinkUpdateRepository linkUpdateRepository;
    private final UpdateNotificationService updateNotificationService;
    private final AvroMapper avroMapper;

    @KafkaListener(topics = "${app.kafka.link-update-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<Long, LinkUpdateAvroMessage> consumerRecord, Acknowledgment ack) {
        log.info(
                "RECEIVED topic={}, key={}, value={} class={}",
                consumerRecord.topic(),
                consumerRecord.key(),
                consumerRecord.value(),
                consumerRecord.value().getClass().getName());

        LinkUpdateMessage message = avroMapper.fromAvro(consumerRecord.value());
        if (consumerRecord.value() == null) {
            log.error("Deserialization failed");
            return;
        }

        if (linkUpdateRepository.existsByEventId(message.eventId())) {
            log.warn("Link get more than once, but not processed");
            ack.acknowledge();
            return;
        }

        log.debug("Consumer consume the message: {}", consumerRecord.value());
        try {
            updateNotificationService.notifyUsers(message);

            linkUpdateRepository.save(new LinkUpdate(message.eventId()));

            ack.acknowledge();
        } catch (NotificationFailedException _) {
            throw new RetryableException("Notification to tg failed");
        }
    }
}
