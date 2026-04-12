package backend.academy.linktracker.scrapper.mapper.database;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.value.LinkId;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public final class LinkRowMapper implements RowMapper<Link> {

    @Override
    public Link mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Link.builder()
                .linkId(new LinkId(rs.getLong("link_id")))
                .url(rs.getString("url"))
                .lastUpdate(rs.getObject("last_update", OffsetDateTime.class))
                .build();
    }
}
