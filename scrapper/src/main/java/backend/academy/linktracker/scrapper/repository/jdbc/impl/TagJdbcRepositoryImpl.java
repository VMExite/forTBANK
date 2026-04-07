package backend.academy.linktracker.scrapper.repository.jdbc.impl;

import backend.academy.linktracker.scrapper.mapper.database.LinkRowMapper;
import backend.academy.linktracker.scrapper.mapper.database.TagRowMapper;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.jdbc.TagJdbcRepository;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final LinkRowMapper linkRowMapper;

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

    private static final String FIND_LINKS_BY_TAG_IDS_TEMPLATE = """
        SELECT lt.tag_id, l.link_id, l.url, l.last_update
        FROM link_tag lt
        JOIN link l ON l.link_id = lt.link_id
        WHERE lt.tag_id IN (%s)
        """;

    @Override
    public List<Tag> findAll() {
        List<Tag> tags = jdbcTemplate.query(FIND_ALL_SQL, tagRowMapper);
        return enrichTags(tags);
    }

    @Override
    public Optional<Tag> findById(Long id) {
        Optional<Tag> tag =
                jdbcTemplate.query(FIND_BY_ID_SQL, tagRowMapper, id).stream().findFirst();

        tag.ifPresent(this::enrichTag);
        return tag;
    }

    @Override
    @Transactional
    public Tag save(Tag entity) {
        if (entity.getTagId() == null) {
            Long id = jdbcTemplate.queryForObject(INSERT_SQL, Long.class, entity.getName());
            entity.setTagId(Objects.requireNonNull(id));
        } else {
            jdbcTemplate.update(UPDATE_SQL, entity.getName(), entity.getTagId());
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
        jdbcTemplate.update(DELETE_SQL, id);
    }

    private List<Tag> enrichTags(List<Tag> tags) {
        if (tags.isEmpty()) {
            return tags;
        }

        List<Long> tagIds = tags.stream().map(Tag::getTagId).toList();

        Map<Long, Set<Link>> linksMap = loadLinks(tagIds);

        for (Tag tag : tags) {
            tag.setLinks(linksMap.getOrDefault(tag.getTagId(), new HashSet<>()));
        }

        return tags;
    }

    private void enrichTag(Tag tag) {
        Long tagId = tag.getTagId();

        Map<Long, Set<Link>> links = loadLinks(List.of(tagId));

        tag.setLinks(links.getOrDefault(tagId, new HashSet<>()));
    }

    private Map<Long, Set<Link>> loadLinks(List<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return new HashMap<>();
        }

        String inSql = tagIds.stream().map(id -> "?").collect(Collectors.joining(","));

        String sql = FIND_LINKS_BY_TAG_IDS_TEMPLATE.formatted(inSql);

        Map<Long, Set<Link>> result = new HashMap<>();

        jdbcTemplate.query(
                sql,
                rs -> {
                    Long tagId = rs.getLong("tag_id");

                    Link link = linkRowMapper.mapRow(rs, 0);

                    result.computeIfAbsent(tagId, k -> new HashSet<>()).add(link);
                },
                tagIds.toArray());

        return result;
    }
}
