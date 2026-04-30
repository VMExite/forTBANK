package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.model.EventStatus;
import backend.academy.linktracker.scrapper.model.entity.OutboxEventEntity;
import jakarta.persistence.LockModeType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT e FROM OutboxEventEntity e
        WHERE e.status = :status
          AND e.retryTime <= :retryTime
          AND e.retryCount <= :maxRetries
        ORDER BY e.retryTime
        """)
    List<OutboxEventEntity> findByStatusAndRetryTime(
        @Param("status") EventStatus status,
        @Param("retryTime") OffsetDateTime retryTime,
        @Param("maxRetries") Integer maxRetries,
        Pageable pageable
    );

    @Modifying
    @Query("UPDATE OutboxEventEntity e SET e.status = :status WHERE e.eventId IN :ids")
    void markSuccess(@Param("ids") List<Long> ids, @Param("status") EventStatus status);
}
