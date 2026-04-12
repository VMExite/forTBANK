package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.mapper.database.LinkRowMapper;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class LinkJdbcAdapter implements LinkRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final LinkRowMapper linkRowMapper;

    private static final String SELECT_BATCH = """
        SELECT l.link_id, l.last_update, l.url
        FROM link l
        WHERE l.last_update < :before
        ORDER BY l.last_update ASC
        LIMIT :size
        """;

    private static final String UPDATE = """
        UPDATE link SET last_update=:last_update WHERE link_id=:link_id
        """;

    @Override
    public List<Link> findBatch(int size, OffsetDateTime before) {
        SqlParameterSource parameters =
                new MapSqlParameterSource().addValue("before", before).addValue("size", size);

        return jdbcTemplate.query(SELECT_BATCH, parameters, linkRowMapper);
    }

    @Override
    public void updateAll(List<Link> links) {
        SqlParameterSource[] parameters = new MapSqlParameterSource[links.size()];
        for (int i = 0; i < links.size(); i++) {
            parameters[i] = new MapSqlParameterSource()
                .addValue("last_update", links.get(i).getLastUpdate())
                .addValue("link_id", links.get(i).getLinkId().value());
        }
        jdbcTemplate.batchUpdate(UPDATE, parameters);
    }
}
