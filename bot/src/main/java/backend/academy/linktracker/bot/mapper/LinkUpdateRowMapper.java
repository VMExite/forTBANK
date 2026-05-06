package backend.academy.linktracker.bot.mapper;

import backend.academy.linktracker.bot.model.LinkUpdate;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class LinkUpdateRowMapper implements RowMapper<LinkUpdate> {
    @Override
    public LinkUpdate mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new LinkUpdate(rs.getLong("event_id"));
    }
}
