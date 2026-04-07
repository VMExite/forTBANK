package backend.academy.linktracker.scrapper.repository.jdbc.impl;

import backend.academy.linktracker.scrapper.mapper.database.LinkRowMapper;
import backend.academy.linktracker.scrapper.mapper.database.TagRowMapper;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.jdbc.TagJdbcRepository;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
@RequiredArgsConstructor
public class TagJdbcRepositoryImpl implements TagJdbcRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TagRowMapper tagRowMapper;
    private final LinkRowMapper linkRowMapper;

    private static final String FIND_ALL_SQL = """
        SELECT tag_id, name
        FROM tag
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT tag_id, name
        FROM tag
        WHERE tag_id = :id
        """;

    private static final String INSERT_SQL = """
        INSERT INTO tag (name)
        VALUES (:name)
        RETURNING tag_id
        """;

    private static final String UPDATE_SQL = """
        UPDATE tag
        SET name = :name
        WHERE tag_id = :id
        """;

    private static final String DELETE_SQL = """
        DELETE FROM tag
        WHERE tag_id = :id
        """;

    private static final String FIND_LINKS_BY_TAG_IDS_SQL = """
        SELECT lt.tag_id, l.link_id, l.url, l.last_update
        FROM link_tag lt
        JOIN link l ON l.link_id = lt.link_id
        WHERE lt.tag_id IN (:ids)
        """;

    private static final String DELETE_ALL_LINK_TAGS_BY_TAG_SQL = """
        DELETE FROM link_tag
        WHERE tag_id = :id
        """;

    @Override
    public List<Tag> findAll() {
        List<Tag> tags = jdbcTemplate.getJdbcTemplate().query(FIND_ALL_SQL, tagRowMapper);
        return enrichTags(tags);
    }

    @Override
    public Optional<Tag> findById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);

        Optional<Tag> tag = jdbcTemplate.query(FIND_BY_ID_SQL, params, tagRowMapper).stream()
                .findFirst();

        tag.ifPresent(this::enrichTag);
        return tag;
    }

    @Override
    @Transactional
    public Tag save(Tag entity) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("name", entity.getName());

        if (entity.getTagId() == null) {
            Long id = jdbcTemplate.queryForObject(INSERT_SQL, params, Long.class);
            entity.setTagId(id);
        } else {
            params.addValue("id", entity.getTagId());
            jdbcTemplate.update(UPDATE_SQL, params);
        }

        return entity;
    }

    @Override
    @Transactional
    public void delete(Tag entity) {
        deleteById(entity.getTagId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(DELETE_ALL_LINK_TAGS_BY_TAG_SQL, params);
        jdbcTemplate.update(DELETE_SQL, params);
    }

    private List<Tag> enrichTags(List<Tag> tags) {
        if (tags.isEmpty()) return tags;

        List<Long> tagIds = tags.stream().map(Tag::getTagId).toList();
        Map<Long, Set<Link>> linksMap = loadLinks(tagIds);

        for (Tag tag : tags) {
            tag.setLinks(new HashSet<>(linksMap.getOrDefault(tag.getTagId(), Collections.emptySet())));
        }

        return tags;
    }

    private void enrichTag(Tag tag) {
        Map<Long, Set<Link>> links = loadLinks(List.of(tag.getTagId()));
        tag.setLinks(new HashSet<>(links.getOrDefault(tag.getTagId(), Collections.emptySet())));
    }

    private Map<Long, Set<Link>> loadLinks(List<Long> tagIds) {
        if (tagIds.isEmpty()) return new HashMap<>();

        MapSqlParameterSource params = new MapSqlParameterSource("ids", tagIds);

        Map<Long, Set<Link>> result = new HashMap<>();

        jdbcTemplate.query(FIND_LINKS_BY_TAG_IDS_SQL, params, rs -> {
            Long tagId = rs.getLong("tag_id");
            Link link = linkRowMapper.mapRow(rs, 0);

            result.computeIfAbsent(tagId, k -> new HashSet<>()).add(link);
        });

        return result;
    }
}
