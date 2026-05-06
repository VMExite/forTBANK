package backend.academy.linktracker.scrapper.service.crud.impl;

import backend.academy.linktracker.scrapper.model.OutboxEvent;
import backend.academy.linktracker.scrapper.model.value.EventId;
import backend.academy.linktracker.scrapper.repository.OutboxEventRepository;
import backend.academy.linktracker.scrapper.service.crud.OutboxEventService;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventServiceImpl implements OutboxEventService {
    private final OutboxEventRepository outboxEventRepository;

    @Override
    @Transactional
    public void save(OutboxEvent entity) {
        if (entity == null) {
            return;
        }
        outboxEventRepository.save(entity);
        log.info("OUTBOX SERVICE SAVE: {}", entity);
    }

    @Override
    @Transactional
    public void save(Collection<OutboxEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        outboxEventRepository.save(events);
        log.info("OUTBOX SERVICE SAVE: {} events", events.size());
    }

    @Override
    @Transactional
    public List<OutboxEvent> getBatch(int batchSize) {
        if (batchSize <= 0) {
            return Collections.emptyList();
        }
        log.info("OUTBOX SERVICE GET BATCH: {}", batchSize);
        return outboxEventRepository.claimBatch(batchSize);
    }

    @Override
    @Transactional
    public void markSuccess(Collection<EventId> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return;
        }
        outboxEventRepository.markSuccess(eventIds);
        log.info("OUTBOX SERVICE MARK SUCCESS: {}", eventIds);
    }

    @Override
    @Transactional
    public void markRetry(EventId eventId, int currentRetryCount, int maxRetry) {
        if (eventId == null) {
            return;
        }
        outboxEventRepository.markRetry(eventId, currentRetryCount, maxRetry);
        log.info("OUTBOX SERVICE MARK RETRY: {}", eventId);
    }
}
