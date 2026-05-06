package backend.academy.linktracker.bot.repository.impl;

import backend.academy.linktracker.bot.mapper.LinkUpdateRowMapper;
import backend.academy.linktracker.bot.model.LinkUpdate;
import backend.academy.linktracker.bot.repository.LinkUpdateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcLinkUpdateAdapter implements LinkUpdateRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final LinkUpdateRowMapper rowMapper;

    private static final String SQL_SAVE = """
        INSERT INTO link_update(event_id)
        VALUES (:eventId)
        ON CONFLICT DO NOTHING
        """;
    private static final String EXISTS_BY_EVENT_ID = """
        SELECT EXISTS
        (SELECT 1 FROM link_update WHERE event_id = :eventId)
    """;

    @Override
    public void save(LinkUpdate entity) {
        log.info("save link update: {}", entity);
        SqlParameterSource parameterSource = new MapSqlParameterSource().addValue("eventId", entity.eventId());

        jdbcTemplate.update(SQL_SAVE, parameterSource);
    }

    @Override
    public boolean existsByEventId(Long eventId) {
        log.info("exists link update: {}", eventId);
        SqlParameterSource parameterSource = new MapSqlParameterSource().addValue("eventId", eventId);

        Boolean exists = jdbcTemplate.queryForObject(EXISTS_BY_EVENT_ID, parameterSource, Boolean.class);

        return Boolean.TRUE.equals(exists);
    }
}
