package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.mapper.database.OutboxRowMapper;
import backend.academy.linktracker.scrapper.model.EventStatus;
import backend.academy.linktracker.scrapper.model.OutboxEvent;
import backend.academy.linktracker.scrapper.model.value.EventId;
import backend.academy.linktracker.scrapper.properties.OutboxProperties;
import backend.academy.linktracker.scrapper.repository.OutboxEventRepository;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    private static final String SQL_CLAIM_BATCH = """
        UPDATE outbox_event
        SET status = 'IN_PROGRESS'
        WHERE event_id IN (
            SELECT event_id FROM outbox_event
            WHERE status = 'NEW'
              AND retry_time <= now()
              AND retry_count <= ?
            ORDER BY retry_time
            LIMIT ?
            FOR UPDATE SKIP LOCKED
        )
        RETURNING *
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

    private static final String SQL_MARK_FAILURE = """
        UPDATE outbox_event
        SET status      = ?,
            retry_count = ?
        WHERE event_id = ?
        """;

    private static final String SQL_RESET_STUCK = """
        UPDATE outbox_event
        SET status = 'NEW'
        WHERE status = 'IN_PROGRESS'
          AND retry_time < now() - (? * interval '1 second')
        """;

    @Override
    @Transactional
    public void save(OutboxEvent event) {
        jdbcTemplate.update(
                SQL_INSERT_EVENT,
                event.getPayload(),
                event.getStatus().name(),
                event.getCreatedAt(),
                event.getRetryCount(),
                event.getRetryTime(),
                event.getType().name());
    }

    @Override
    @Transactional
    public void save(Collection<OutboxEvent> events) {
        List<Object[]> args = events.stream()
                .map(e -> new Object[] {
                    e.getPayload(),
                    e.getStatus().name(),
                    e.getCreatedAt(),
                    e.getRetryCount(),
                    e.getRetryTime(),
                    e.getType().name()
                })
                .toList();

        jdbcTemplate.batchUpdate(SQL_INSERT_EVENT, args);
    }

    @Override
    @Transactional
    public List<OutboxEvent> claimBatch(int batchSize) {
        return jdbcTemplate.query(SQL_CLAIM_BATCH, rowMapper, properties.getMaxRetry(), batchSize);
    }

    @Override
    @Transactional
    public void markSuccess(Collection<EventId> eventIds) {
        Long[] ids = eventIds.stream().map(EventId::id).toArray(Long[]::new);

        jdbcTemplate.update(SQL_MARK_SUCCESS, EventStatus.SUCCESS.name(), ids);
    }

    @Override
    @Transactional
    public void markRetry(EventId eventId, int currentRetryCount, int maxRetry) {
        if (currentRetryCount >= maxRetry) {
            jdbcTemplate.update(SQL_MARK_FAILURE, EventStatus.FAILURE.name(), currentRetryCount, eventId.id());
            return;
        }

        OffsetDateTime nextRetry = OffsetDateTime.now().plusSeconds(retryBackoff(currentRetryCount));

        jdbcTemplate.update(SQL_MARK_RETRY, currentRetryCount + 1, nextRetry, EventStatus.NEW.name(), eventId.id());
    }

    @Transactional
    public void resetStuck(long stuckAfterSeconds) {
        jdbcTemplate.update(SQL_RESET_STUCK, stuckAfterSeconds);
    }

    private long retryBackoff(int retryCount) {
        return 10L * (1L << retryCount);
    }
}
