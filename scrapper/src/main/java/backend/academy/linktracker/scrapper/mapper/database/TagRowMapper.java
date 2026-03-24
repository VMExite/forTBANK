package backend.academy.linktracker.scrapper.mapper.database;

import backend.academy.linktracker.scrapper.model.Tag;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class TagRowMapper implements RowMapper<Tag> {
    @Override
    public Tag mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Tag.builder()
                .tagId(rs.getLong("tag_id"))
                .name(rs.getString("name"))
                .build();
    }
}
