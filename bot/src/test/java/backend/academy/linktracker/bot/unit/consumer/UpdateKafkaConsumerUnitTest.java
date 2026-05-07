package backend.academy.linktracker.bot.unit.consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.consumer.UpdateKafkaConsumer;
import backend.academy.linktracker.bot.dto.LinkUpdateMessage;
import backend.academy.linktracker.bot.exception.NotificationFailedException;
import backend.academy.linktracker.bot.exception.RetryableException;
import backend.academy.linktracker.bot.mapper.AvroMapper;
import backend.academy.linktracker.bot.model.LinkUpdate;
import backend.academy.linktracker.bot.repository.LinkUpdateRepository;
import backend.academy.linktracker.bot.service.UpdateNotificationService;
import backend.academy.linktracker.dto.kafka.LinkUpdateAvroMessage;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
class UpdateKafkaConsumerUnitTest {

    @Mock
    private LinkUpdateRepository linkUpdateRepository;

    @Mock
    private UpdateNotificationService updateNotificationService;

    @Mock
    private AvroMapper avroMapper;

    @InjectMocks
    private UpdateKafkaConsumer consumer;

    private static final String TOPIC = "linkUpdateEvent";
    private static final int PARTITION = 0;
    private static final long OFFSET = 0L;

    private LinkUpdateAvroMessage buildAvroMessage(long eventId, long chatId) {
        return LinkUpdateAvroMessage.newBuilder()
            .setEventId(eventId)
            .setChatId(chatId)
            .setLinkId(1L)
            .setTitle("Test Title")
            .setUsername("test_user")
            .setPreview("Test preview")
            .setUrl("https://github.com/test/repo")
            .setCreatedAt(Instant.now())
            .build();
    }

    private LinkUpdateMessage buildMessage(long eventId, long chatId) {
        return new LinkUpdateMessage(
            eventId, chatId, 1L, "Test Title", "test_user", "Test preview",
            "https://github.com/test/repo", OffsetDateTime.now());
    }

    @Test
    void shouldProcessValidMessageSuccessfully() throws NotificationFailedException {
        long eventId = 1L;
        long chatId = 42L;

        LinkUpdateAvroMessage avroMessage = buildAvroMessage(eventId, chatId);
        LinkUpdateMessage message = buildMessage(eventId, chatId);
        ConsumerRecord<Long, LinkUpdateAvroMessage> record =
            new ConsumerRecord<>(TOPIC, PARTITION, OFFSET, chatId, avroMessage);
        Acknowledgment ack = mock(Acknowledgment.class);

        when(avroMapper.fromAvro(avroMessage)).thenReturn(message);
        when(linkUpdateRepository.existsByEventId(eventId)).thenReturn(false);

        consumer.consume(record, ack);

        verify(updateNotificationService).notifyUsers(message);
        verify(linkUpdateRepository).save(new LinkUpdate(eventId));
        verify(ack).acknowledge();
    }

    @Test
    void shouldSkipDuplicateMessage() throws NotificationFailedException {
        long eventId = 2L;
        long chatId = 42L;

        LinkUpdateAvroMessage avroMessage = buildAvroMessage(eventId, chatId);
        LinkUpdateMessage message = buildMessage(eventId, chatId);
        ConsumerRecord<Long, LinkUpdateAvroMessage> record =
            new ConsumerRecord<>(TOPIC, PARTITION, OFFSET, chatId, avroMessage);
        Acknowledgment ack = mock(Acknowledgment.class);

        when(avroMapper.fromAvro(avroMessage)).thenReturn(message);
        when(linkUpdateRepository.existsByEventId(eventId)).thenReturn(true);

        consumer.consume(record, ack);

        verify(updateNotificationService, never()).notifyUsers(any());
        verify(ack).acknowledge();
    }

    @Test
    void shouldThrowRetryableExceptionWhenNotificationFails() throws NotificationFailedException {
        long eventId = 3L;
        long chatId = 42L;

        LinkUpdateAvroMessage avroMessage = buildAvroMessage(eventId, chatId);
        LinkUpdateMessage message = buildMessage(eventId, chatId);
        ConsumerRecord<Long, LinkUpdateAvroMessage> record =
            new ConsumerRecord<>(TOPIC, PARTITION, OFFSET, chatId, avroMessage);
        Acknowledgment ack = mock(Acknowledgment.class);

        when(avroMapper.fromAvro(avroMessage)).thenReturn(message);
        when(linkUpdateRepository.existsByEventId(eventId)).thenReturn(false);
        doThrow(new NotificationFailedException("Telegram failed")).when(updateNotificationService).notifyUsers(message);

        assertThrows(RetryableException.class, () -> consumer.consume(record, ack));

        verify(linkUpdateRepository, never()).save(any());
        verify(ack, never()).acknowledge();
    }

    @Test
    void shouldNotAcknowledgeOnNotificationFailure() throws NotificationFailedException {
        long eventId = 4L;
        long chatId = 42L;

        LinkUpdateAvroMessage avroMessage = buildAvroMessage(eventId, chatId);
        LinkUpdateMessage message = buildMessage(eventId, chatId);
        ConsumerRecord<Long, LinkUpdateAvroMessage> record =
            new ConsumerRecord<>(TOPIC, PARTITION, OFFSET, chatId, avroMessage);
        Acknowledgment ack = mock(Acknowledgment.class);

        when(avroMapper.fromAvro(avroMessage)).thenReturn(message);
        when(linkUpdateRepository.existsByEventId(eventId)).thenReturn(false);
        doThrow(new NotificationFailedException("fail")).when(updateNotificationService).notifyUsers(message);

        try {
            consumer.consume(record, ack);
        } catch (RetryableException ignored) {
        }

        verify(ack, never()).acknowledge();
    }

    @Test
    void shouldReturnEarlyIfNullAvroValue() {
        ConsumerRecord<Long, LinkUpdateAvroMessage> record =
            new ConsumerRecord<>(TOPIC, PARTITION, OFFSET, 1L, null);
        Acknowledgment ack = mock(Acknowledgment.class);

        consumer.consume(record, ack);

        verify(updateNotificationService, never()).notifyUsers(any());
        verify(ack, never()).acknowledge();
    }
}
