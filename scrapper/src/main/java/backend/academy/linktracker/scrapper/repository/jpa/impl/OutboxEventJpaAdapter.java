package backend.academy.linktracker.scrapper.repository.jpa.impl;

import backend.academy.linktracker.scrapper.mapper.OutboxEventMapper;
import backend.academy.linktracker.scrapper.model.EventStatus;
import backend.academy.linktracker.scrapper.model.OutboxEvent;
import backend.academy.linktracker.scrapper.model.entity.OutboxEventEntity;
import backend.academy.linktracker.scrapper.model.value.EventId;
import backend.academy.linktracker.scrapper.properties.OutboxProperties;
import backend.academy.linktracker.scrapper.repository.OutboxEventRepository;
import backend.academy.linktracker.scrapper.repository.jpa.OutboxEventJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OutboxEventJpaAdapter implements OutboxEventRepository {
    private final OutboxEventJpaRepository repository;
    private final OutboxEventMapper mapper;
    private final OutboxProperties properties;

    @Override
    @Transactional
    public void save(OutboxEvent model) {
        boolean isNew = model.getEventId() == null || model.getEventId().id() == null;
        if (isNew) {
            repository.save(mapper.toEntity(model));
            return;
        }

        OutboxEventEntity entity = repository.findById(model.getEventId().id())
            .orElseGet(() -> mapper.toEntity(model));

        entity.setStatus(model.getStatus());
        entity.setRetryCount(model.getRetryCount());
        entity.setRetryTime(model.getRetryTime());
        repository.save(entity);
    }

    @Override
    @Transactional
    public void save(Collection<OutboxEvent> models) {
        List<OutboxEventEntity> entities = models.stream()
            .map(mapper::toEntity)
            .toList();

        repository.saveAll(entities);
    }

    @Override
    @Transactional
    public List<OutboxEvent> claimBatch(int batchSize) {
        Pageable pageable = PageRequest.of(0, batchSize);
        return repository
            .findByStatusAndRetryTime(
                EventStatus.NEW,
                OffsetDateTime.now(),
                properties.getMaxRetry(),
                pageable
            )
            .stream()
            .map(mapper::fromEntity)
            .toList();
    }

    @Override
    @Transactional
    public void markSuccess(Collection<EventId> eventIds) {
        List<Long> ids = eventIds.stream().map(EventId::id).toList();
        repository.markSuccess(ids, EventStatus.SUCCESS);
    }

    @Override
    @Transactional
    public void markRetry(EventId eventId, int currentRetryCount, int maxRetry) {
        OutboxEventEntity entity = repository.findById(eventId.id())
            .orElseThrow(() -> new EntityNotFoundException("OutboxEvent not found: " + eventId));

        if (currentRetryCount >= maxRetry) {
            entity.setStatus(EventStatus.FAILURE);
            entity.setRetryCount(currentRetryCount);
        } else {
            entity.setStatus(EventStatus.NEW);
            entity.setRetryCount(currentRetryCount + 1);
            entity.setRetryTime(OffsetDateTime.now().plusSeconds(retryBackoff(currentRetryCount)));
        }
    }

    private long retryBackoff(int retryCount) {
        return 10L * (1L << retryCount);
    }
}
