package backend.academy.linktracker.scrapper.sheduler;

import backend.academy.linktracker.scrapper.model.OutboxEvent;
import backend.academy.linktracker.scrapper.properties.OutboxProperties;
import backend.academy.linktracker.scrapper.service.crud.OutboxEventService;
import backend.academy.linktracker.scrapper.sheduler.processing.impl.OutboxEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {
    private final OutboxEventProcessor outboxEventProcessor;
    private final OutboxEventService outboxEventService;
    private final OutboxProperties properties;

    @Scheduled(fixedDelayString = "${app.scheduler.delay}")
    public void send() {
        List<OutboxEvent> events = outboxEventService.getBatch(properties.getBatchSize());

        if (!events.isEmpty()) {
            outboxEventProcessor.process(events);
        }
    }
}
