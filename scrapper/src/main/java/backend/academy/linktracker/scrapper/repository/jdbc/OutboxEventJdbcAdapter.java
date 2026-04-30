package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.mapper.database.OutboxRowMapper;
import backend.academy.linktracker.scrapper.model.EventStatus;
import backend.academy.linktracker.scrapper.model.OutboxEvent;
import backend.academy.linktracker.scrapper.model.value.EventId;
import backend.academy.linktracker.scrapper.properties.OutboxProperties;
import backend.academy.linktracker.scrapper.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
@Repository
@RequiredArgsConstructor
public class OutboxEventJdbcAdapter implements OutboxEventRepository {
    private final JdbcTemplate jdbcTemplate;
    private final OutboxRowMapper rowMapper;
    private final OutboxProperties properties;

    private static final String SQL_INSERT_EVENT = """
        INSERT INTO outbox_event (payload, status, created_at, retry_count, retry_time, type)
        VALUES (?::jsonb, ?, ?, ?, ?, ?)
        """;

    private static final String SQL_INSERT_BATCH = """
        INSERT INTO outbox_event (payload, status, created_at, retry_count, retry_time, type)
        VALUES (?::jsonb, ?, ?, ?, ?, ?)
        """;

    private static final String SQL_CLAIM_BATCH = """
        SELECT * FROM outbox_event
        WHERE status = ?
          AND retry_time <= now()
          AND retry_count <= ?
        ORDER BY retry_time
        LIMIT ?
        FOR UPDATE SKIP LOCKED
        """;

    private static final String SQL_MARK_SUCCESS = """
        UPDATE outbox_event
        SET status = ?
        WHERE event_id = ANY(?)
        """;

    private static final String SQL_MARK_RETRY = """
        UPDATE outbox_event
        SET retry_count = ?,
            retry_time  = ?,
            status      = ?
        WHERE event_id = ?
        """;

    private static final String SQL_UPDATE_EVENT = """
        UPDATE outbox_event
        SET status = ?, retry_count = ?
        WHERE event_id = ?
        """;

    @Override
    @Transactional
    public void save(OutboxEvent event) {
        jdbcTemplate.update(SQL_INSERT_EVENT,
            event.getPayload(),
            event.getStatus().name(),
            event.getCreatedAt(),
            event.getRetryCount(),
            event.getRetryTime(),
            event.getType().name()
        );
    }

    @Override
    @Transactional
    public void save(Collection<OutboxEvent> events) {
        List<Object[]> args = events.stream()
            .map(e -> new Object[]{
                e.getPayload(),
                e.getStatus().name(),
                e.getCreatedAt(),
                e.getRetryCount(),
                e.getRetryTime(),
                e.getType().name()
            })
            .toList();

        jdbcTemplate.batchUpdate(SQL_INSERT_BATCH, args);
    }

    @Override
    @Transactional
    public List<OutboxEvent> claimBatch(int batchSize) {
        return jdbcTemplate.query(
            SQL_CLAIM_BATCH,
            rowMapper,
            EventStatus.NEW.name(),
            properties.getMaxRetry(),
            batchSize
        );
    }

    @Override
    @Transactional
    public void markSuccess(Collection<EventId> eventIds) {
        Long[] ids = eventIds.stream()
            .map(EventId::id)
            .toArray(Long[]::new);

        jdbcTemplate.update(SQL_MARK_SUCCESS,
            EventStatus.SUCCESS.name(),
            ids
        );
    }

    @Override
    @Transactional
    public void markRetry(EventId eventId, int currentRetryCount, int maxRetry) {
        if (currentRetryCount >= maxRetry) {
            jdbcTemplate.update(
                SQL_UPDATE_EVENT,
                EventStatus.FAILURE.name(),
                currentRetryCount,
                eventId.id()
            );
            return;
        }

        OffsetDateTime nextRetry = OffsetDateTime.now()
            .plusSeconds(retryBackoff(currentRetryCount));

        jdbcTemplate.update(SQL_MARK_RETRY,
            currentRetryCount + 1,
            nextRetry,
            EventStatus.NEW.name(),
            eventId.id()
        );
    }

    // calculate backoff exponential
    private long retryBackoff(int retryCount) {
        return 10L * (1L << retryCount);
    }
}
