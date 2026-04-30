package backend.academy.linktracker.scrapper.model;

import backend.academy.linktracker.scrapper.model.value.EventId;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Builder
public class OutboxEvent {
    @Getter
    private EventId eventId;

    @Getter
    private String payload;

    @Getter
    @Setter
    private EventStatus status;

    @Getter
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Getter
    @Setter
    private OffsetDateTime retryTime;

    @Getter
    @Setter
    private Integer retryCount;

    @Getter
    private EventType type;
}
