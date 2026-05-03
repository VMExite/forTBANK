package backend.academy.linktracker.scrapper.sheduler.processing.impl;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.mapper.OutboxEventMapper;
import backend.academy.linktracker.scrapper.model.OutboxEvent;
import backend.academy.linktracker.scrapper.model.value.EventId;
import backend.academy.linktracker.scrapper.properties.OutboxProperties;
import backend.academy.linktracker.scrapper.service.crud.OutboxEventService;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import backend.academy.linktracker.scrapper.sheduler.processing.SingleThreadProcessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OutboxEventProcessor implements SingleThreadProcessor<OutboxEvent> {
    private final ExecutorService executor;
    private final MessageSender messageSender;
    private final OutboxProperties properties;
    private final OutboxEventService outboxEventService;
    private final OutboxEventMapper outboxEventMapper;

    public OutboxEventProcessor(
            @Qualifier("outboxExecutor") ExecutorService executor,
            MessageSender messageSender,
            OutboxProperties properties,
            OutboxEventService outboxEventService,
            OutboxEventMapper outboxEventMapper) {
        this.executor = executor;
        this.messageSender = messageSender;
        this.properties = properties;
        this.outboxEventService = outboxEventService;
        this.outboxEventMapper = outboxEventMapper;
    }

    @Override
    public void process(Collection<OutboxEvent> events) {
        List<EventId> successIds = Collections.synchronizedList(new ArrayList<>());

        List<CompletableFuture<Void>> futures = events.stream()
                .map(e -> CompletableFuture.runAsync(() -> processEvent(e, successIds), executor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        outboxEventService.markSuccess(successIds);
    }

    private void processEvent(OutboxEvent event, Collection<EventId> successIds) {
        int maxRetry = properties.getMaxRetry();
        try {
            LinkUpdateMessage message = outboxEventMapper.toMessage(event);
            messageSender.sendMessage(message);
            successIds.add(event.getEventId());
            log.info("Message {} sent to {}", message, event.getType());
        } catch (Exception _) {
            log.warn("Fail to send message {}, retry {}/{}", event.getEventId(), event.getRetryCount(), maxRetry);
            outboxEventService.markRetry(event.getEventId(), event.getRetryCount(), maxRetry);
        }
    }
}
