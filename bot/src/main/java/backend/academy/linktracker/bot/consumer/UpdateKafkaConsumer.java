package backend.academy.linktracker.bot.consumer;

import backend.academy.linktracker.bot.dto.LinkUpdateMessage;
import backend.academy.linktracker.bot.service.UpdateNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
// kafka consumer
public class UpdateKafkaConsumer {
    private final UpdateNotificationService updateNotificationService;

    @KafkaListener(topics = "linkUpdateEvent", groupId = "linkUpdateGroup")
    public void consume(LinkUpdateMessage message) {
        log.debug("Consumer consume the message: {}", message);
        updateNotificationService.notifyUsers(message);
    }
}
