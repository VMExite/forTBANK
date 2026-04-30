package backend.academy.linktracker.scrapper.mapper.database;

import backend.academy.linktracker.scrapper.model.EventStatus;
import backend.academy.linktracker.scrapper.model.EventType;
import backend.academy.linktracker.scrapper.model.OutboxEvent;
import backend.academy.linktracker.scrapper.model.value.EventId;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

@Component
public class OutboxRowMapper implements RowMapper<OutboxEvent> {
    @Override
    public OutboxEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return OutboxEvent.builder()
            .eventId(new EventId(rs.getLong("event_id")))
            .payload(rs.getString("payload"))
            .status(EventStatus.valueOf(rs.getString("status")))
            .createdAt(rs.getObject("created_at", OffsetDateTime.class))
            .retryCount(rs.getInt("retry_count"))
            .retryTime(rs.getObject("retry_time", OffsetDateTime.class))
            .type(EventType.valueOf(rs.getString("type")))
            .build();
    }
}
