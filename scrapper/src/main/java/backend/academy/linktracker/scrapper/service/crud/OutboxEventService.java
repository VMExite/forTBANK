package backend.academy.linktracker.scrapper.service.crud;

import backend.academy.linktracker.scrapper.model.OutboxEvent;
import backend.academy.linktracker.scrapper.model.value.EventId;
import java.util.Collection;
import java.util.List;

public interface OutboxEventService {
    void save(OutboxEvent event);

    void save(Collection<OutboxEvent> events);

    List<OutboxEvent> getBatch(int batchSize);

    void markSuccess(Collection<EventId> eventIds);

    void markRetry(EventId eventId, int currentRetryCount, int maxRetry);
}
