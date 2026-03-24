package backend.academy.linktracker.scrapper.mapper.database;

import backend.academy.linktracker.scrapper.model.Link;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class LinkRowMapper implements RowMapper<Link> {
    @Override
    public Link mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Link.builder()
                .linkId(rs.getLong("link_id"))
                .url(rs.getString("url"))
                .build();
    }
}
