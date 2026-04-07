package backend.academy.linktracker.scrapper.repository.jdbc.impl;

import backend.academy.linktracker.scrapper.mapper.database.LinkRowMapper;
import backend.academy.linktracker.scrapper.mapper.database.TagRowMapper;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.jdbc.LinkJdbcRepository;
import jakarta.transaction.Transactional;
import java.util.Collections;
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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
@RequiredArgsConstructor
public class LinkJdbcRepositoryImpl implements LinkJdbcRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final LinkRowMapper linkRowMapper;
    private final TagRowMapper tagRowMapper;

    private static final String FIND_ALL_SQL = """
        SELECT link_id, url, last_update
        FROM link
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT link_id, url, last_update
        FROM link
        WHERE link_id = :id
        """;

    private static final String FIND_BY_URL_SQL = """
        SELECT link_id, url, last_update
        FROM link
        WHERE url = :url
    """;

    private static final String INSERT_SQL = """
        INSERT INTO link (url, last_update)
        VALUES (:url, :lastUpdate)
        RETURNING link_id
        """;

    private static final String UPDATE_SQL = """
        UPDATE link
        SET url = :url,
            last_update = :lastUpdate
        WHERE link_id = :id
        """;

    private static final String DELETE_SQL = """
        DELETE FROM link
        WHERE link_id = :id
        """;

    private static final String FIND_TAGS_BY_LINK_IDS_SQL = """
        SELECT lt.link_id, t.tag_id, t.name
        FROM link_tag lt
        JOIN tag t ON t.tag_id = lt.tag_id
        WHERE lt.link_id IN (:ids)
        """;

    private static final String INSERT_LINK_TAG_SQL = """
        INSERT INTO link_tag (link_id, tag_id)
        VALUES (:linkId, :tagId)
        ON CONFLICT DO NOTHING
        """;

    private static final String DELETE_LINK_TAG_SQL = """
        DELETE FROM link_tag
        WHERE link_id = :linkId AND tag_id = :tagId
        """;

    private static final String DELETE_ALL_LINK_TAGS_SQL = """
        DELETE FROM link_tag
        WHERE link_id = :id
        """;

    private static final String SELECT_TAG_IDS_SQL = """
        SELECT tag_id FROM link_tag WHERE link_id = :id
        """;

    @Override
    public List<Link> findAll() {
        List<Link> links = jdbcTemplate.getJdbcTemplate().query(FIND_ALL_SQL, linkRowMapper);
        return enrichLinks(links);
    }

    @Override
    public Optional<Link> findById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);

        Optional<Link> link = jdbcTemplate.query(FIND_BY_ID_SQL, params, linkRowMapper).stream()
                .findFirst();

        link.ifPresent(this::enrichLink);
        return link;
    }

    @Override
    public List<Link> findLinkByUrl(String url) {
        MapSqlParameterSource params = new MapSqlParameterSource("url", url);

        List<Link> links = jdbcTemplate.query(FIND_BY_URL_SQL, params, linkRowMapper);

        return enrichLinks(links);
    }

    @Override
    @Transactional
    public Link save(Link entity) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("url", entity.getUrl())
                .addValue("lastUpdate", entity.getLastUpdate());

        if (entity.getLinkId() == null) {
            Long id = jdbcTemplate.queryForObject(INSERT_SQL, params, Long.class);
            entity.setLinkId(id);
        } else {
            params.addValue("id", entity.getLinkId());
            jdbcTemplate.update(UPDATE_SQL, params);
        }

        syncTags(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Link entity) {
        deleteById(entity.getLinkId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(DELETE_ALL_LINK_TAGS_SQL, params);
        jdbcTemplate.update(DELETE_SQL, params);
    }

    private void syncTags(Link link) {
        Long linkId = link.getLinkId();

        MapSqlParameterSource params = new MapSqlParameterSource("id", linkId);

        List<Long> existingList = jdbcTemplate.query(SELECT_TAG_IDS_SQL, params, (rs, rowNum) -> rs.getLong("tag_id"));

        Set<Long> existing = new HashSet<>(existingList);

        Set<Long> incoming = link.getTags().stream()
                .map(Tag::getTagId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> toInsert = new HashSet<>(incoming);
        toInsert.removeAll(existing);

        Set<Long> toDelete = new HashSet<>(existing);
        toDelete.removeAll(incoming);

        batchInsert(linkId, toInsert);
        batchDelete(linkId, toDelete);
    }

    private void batchInsert(Long linkId, Set<Long> tagIds) {
        if (tagIds.isEmpty()) return;

        List<MapSqlParameterSource> batch = tagIds.stream()
                .map(tagId ->
                        new MapSqlParameterSource().addValue("linkId", linkId).addValue("tagId", tagId))
                .toList();

        jdbcTemplate.batchUpdate(INSERT_LINK_TAG_SQL, batch.toArray(new MapSqlParameterSource[0]));
    }

    private void batchDelete(Long linkId, Set<Long> tagIds) {
        if (tagIds.isEmpty()) return;

        List<MapSqlParameterSource> batch = tagIds.stream()
                .map(tagId ->
                        new MapSqlParameterSource().addValue("linkId", linkId).addValue("tagId", tagId))
                .toList();

        jdbcTemplate.batchUpdate(DELETE_LINK_TAG_SQL, batch.toArray(new MapSqlParameterSource[0]));
    }

    private List<Link> enrichLinks(List<Link> links) {
        if (links.isEmpty()) return links;

        List<Long> linkIds = links.stream().map(Link::getLinkId).toList();
        Map<Long, Set<Tag>> tagsMap = loadTags(linkIds);

        for (Link link : links) {
            link.setTags(new HashSet<>(tagsMap.getOrDefault(link.getLinkId(), Collections.emptySet())));
        }

        return links;
    }

    private void enrichLink(Link link) {
        Map<Long, Set<Tag>> tags = loadTags(List.of(link.getLinkId()));
        link.setTags(new HashSet<>(tags.getOrDefault(link.getLinkId(), Collections.emptySet())));
    }

    private Map<Long, Set<Tag>> loadTags(List<Long> linkIds) {
        if (linkIds.isEmpty()) return new HashMap<>();

        MapSqlParameterSource params = new MapSqlParameterSource("ids", linkIds);

        Map<Long, Set<Tag>> result = new HashMap<>();

        jdbcTemplate.query(FIND_TAGS_BY_LINK_IDS_SQL, params, rs -> {
            Long linkId = rs.getLong("link_id");
            Tag tag = tagRowMapper.mapRow(rs, 0);

            result.computeIfAbsent(linkId, k -> new HashSet<>()).add(tag);
        });

        return result;
    }
}
