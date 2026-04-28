package backend.academy.linktracker.bot.consumer;

import backend.academy.linktracker.bot.dto.LinkUpdateMessage;
import backend.academy.linktracker.bot.exception.NotRetryableException;
import backend.academy.linktracker.bot.service.UpdateNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
// kafka consumer
public class UpdateKafkaConsumer {
    private final UpdateNotificationService updateNotificationService;

    @KafkaListener(topics = "linkUpdateEvent", groupId = "linkUpdateGroup")
    public void consume(ConsumerRecord<Long, LinkUpdateMessage> consumerRecord) {
        log.info("RECEIVED topic={}, key={}, value={}",
            consumerRecord.topic(),
            consumerRecord.key(),
            consumerRecord.value());
        if (consumerRecord.value() == null) {
            log.info("Consumer fail deserialization: {}", consumerRecord.topic());
            throw new NotRetryableException("Deserialization fail");
        }

        log.debug("Consumer consume the message: {}", consumerRecord.value());
        updateNotificationService.notifyUsers(consumerRecord.value());
    }
}
