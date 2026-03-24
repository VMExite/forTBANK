package backend.academy.linktracker.scrapper.repository.jdbc.impl;

import backend.academy.linktracker.scrapper.mapper.database.TagRowMapper;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.jdbc.TagJdbcRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
@RequiredArgsConstructor
public class TagJdbcRepositoryImpl implements TagJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TagRowMapper tagRowMapper;

    private static final String FIND_ALL_SQL = """
        SELECT tag_id, name
        FROM tag
        """;
    private static final String FIND_BY_ID_SQL = """
        SELECT tag_id, name
        FROM tag
        WHERE tag_id = ?
        """;

    private static final String INSERT_SQL = """
        INSERT INTO tag (name)
        VALUES (?)
        RETURNING tag_id
        """;
    private static final String UPDATE_SQL = """
        UPDATE tag
        SET name = ?
        WHERE tag_id = ?
        """;

    private static final String DELETE_SQL = """
        DELETE FROM tag
        WHERE tag_id = ?
        """;

    @Override
    public List<Tag> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, tagRowMapper);
    }

    @Override
    public Optional<Tag> findById(Long id) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, tagRowMapper, id).stream().findFirst();
    }

    @Override
    public Tag save(Tag entity) {
        if (entity.getTagId() == null) {
            Long id = jdbcTemplate.queryForObject(INSERT_SQL, Long.class, entity.getName());
            entity.setTagId(id);
        } else {
            jdbcTemplate.update(UPDATE_SQL, entity.getName(), entity.getTagId());
        }
        return entity;
    }

    @Override
    public void delete(Tag entity) {
        deleteById(entity.getTagId());
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }
}
