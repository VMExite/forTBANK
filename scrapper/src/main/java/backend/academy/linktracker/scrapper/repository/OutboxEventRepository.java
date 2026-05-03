package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.OutboxEvent;
import backend.academy.linktracker.scrapper.model.value.EventId;
import java.util.Collection;
import java.util.List;

public interface OutboxEventRepository {
    void save(OutboxEvent event);

    void save(Collection<OutboxEvent> events);

    List<OutboxEvent> claimBatch(int batchSize);

    void markSuccess(Collection<EventId> eventIds);

    void markRetry(EventId eventId, int currentRetryCount, int maxRetry);
}
