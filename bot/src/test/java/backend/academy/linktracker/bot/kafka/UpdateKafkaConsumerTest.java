package backend.academy.linktracker.bot.kafka;

import backend.academy.linktracker.bot.consumer.UpdateKafkaConsumer;
import backend.academy.linktracker.bot.dto.LinkUpdateMessage;
import backend.academy.linktracker.bot.exception.NotRetryableException;
import backend.academy.linktracker.bot.service.UpdateNotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateKafkaConsumerTest {
    @Mock
    private UpdateNotificationService notificationService;

    @InjectMocks
    private UpdateKafkaConsumer consumer;

    private static final String KAFKA_TOPIC = "linkUpdateMessage";
    private static final Integer PARTITION = 0;
    private static final Integer OFFSET = 0;
    private static final Long KEY = 1L;

    @Test
    void greenProcessMessageTest() {
        Long longs = 1L;
        String str = "test";
        LinkUpdateMessage message =
            new LinkUpdateMessage(longs,longs, str, str, str, str, OffsetDateTime.now());

        ConsumerRecord<Long, LinkUpdateMessage> consumerRecord = consumerRecord(message);
        consumer.consume(consumerRecord);

        verify(notificationService).notifyUsers(message);

    }

    @Test
    void failProcessMessageTest() throws NotRetryableException {
        ConsumerRecord<Long, LinkUpdateMessage> consumerRecord = consumerRecord(null);

        assertThrows(NotRetryableException.class, () -> consumer.consume(consumerRecord));
    }

    private ConsumerRecord<Long, LinkUpdateMessage> consumerRecord(LinkUpdateMessage message) {
        return new ConsumerRecord<>(KAFKA_TOPIC, PARTITION, OFFSET, KEY, message);
    }
}
