package backend.academy.linktracker.scrapper.repository.jdbc.impl;

import backend.academy.linktracker.scrapper.mapper.database.ChatRowMapper;
import backend.academy.linktracker.scrapper.mapper.database.LinkRowMapper;
import backend.academy.linktracker.scrapper.mapper.database.TagRowMapper;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.jdbc.LinkJdbcRepository;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
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
public class LinkJdbcRepositoryImpl implements LinkJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final LinkRowMapper linkRowMapper;
    private final TagRowMapper tagRowMapper;
    private final ChatRowMapper chatRowMapper;

    private static final String FIND_ALL_SQL = """
        SELECT link_id, url, last_update
        FROM link
        """;
    private static final String FIND_BY_ID_SQL = """
        SELECT link_id, url, last_update
        FROM link
        WHERE link_id = ?
        """;
    private static final String FIND_BY_URL_SQL = """
        SELECT link_id, url, last_update
        FROM link
        WHERE url = ?
        """;
    private static final String INSERT_SQL = """
        INSERT INTO link (url, last_update)
        VALUES (?, ?)
        RETURNING link_id
        """;
    private static final String UPDATE_SQL = """
        UPDATE link
        SET url = ?, last_update = ?
        WHERE link_id = ?
        """;
    private static final String DELETE_SQL = """
        DELETE FROM link
        WHERE link_id = ?
        """;
    private static final String FIND_TAGS_BY_LINK_IDS_TEMPLATE = """
        SELECT lt.link_id, t.tag_id, t.name
        FROM link_tag lt
        JOIN tag t ON t.tag_id = lt.tag_id
        WHERE lt.link_id IN (%s)
        """;
    private static final String FIND_CHATS_BY_LINK_IDS_TEMPLATE = """
        SELECT cl.link_id, c.chat_id
        FROM chat_link cl
        JOIN chat c ON c.chat_id = cl.chat_id
        WHERE cl.link_id IN (%s)
    """;

    @Override
    public List<Link> findAll() {
        List<Link> links = jdbcTemplate.query(FIND_ALL_SQL, linkRowMapper);
        return enrichLinks(links);
    }

    @Override
    public Optional<Link> findById(Long id) {
        Optional<Link> link =
                jdbcTemplate.query(FIND_BY_ID_SQL, linkRowMapper, id).stream().findFirst();

        link.ifPresent(this::enrichLink);
        return link;
    }

    @Override
    public List<Link> findLinkByUrl(String url) {
        List<Link> links = jdbcTemplate.query(FIND_BY_URL_SQL, linkRowMapper, url);
        return enrichLinks(links);
    }

    @Override
    @Transactional
    public Link save(Link entity) {
        if (entity.getLastUpdate() == null) {
            entity.setLastUpdate(OffsetDateTime.now());
        }

        if (entity.getLinkId() == null) {
            Long id = jdbcTemplate.queryForObject(INSERT_SQL, Long.class, entity.getUrl(), entity.getLastUpdate());
            entity.setLinkId(Objects.requireNonNull(id));
        } else {
            jdbcTemplate.update(UPDATE_SQL, entity.getUrl(), entity.getLastUpdate(), entity.getLinkId());
        }

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
        jdbcTemplate.update(DELETE_SQL, id);
    }

    private List<Link> enrichLinks(List<Link> links) {
        if (links.isEmpty()) return links;

        List<Long> linkIds = links.stream().map(Link::getLinkId).toList();

        Map<Long, Set<Tag>> tagsMap = loadTags(linkIds);
        Map<Long, Set<Chat>> chatsMap = loadChats(linkIds);

        for (Link link : links) {
            link.setTags(tagsMap.getOrDefault(link.getLinkId(), new HashSet<>()));
            link.setChats(chatsMap.getOrDefault(link.getLinkId(), new HashSet<>()));
        }

        return links;
    }

    private void enrichLink(Link link) {
        Long linkId = link.getLinkId();

        Map<Long, Set<Tag>> tags = loadTags(List.of(linkId));
        Map<Long, Set<Chat>> chats = loadChats(List.of(linkId));

        link.setTags(tags.getOrDefault(linkId, new HashSet<>()));
        link.setChats(chats.getOrDefault(linkId, new HashSet<>()));
    }

    private Map<Long, Set<Tag>> loadTags(List<Long> linkIds) {
        String inSql = linkIds.stream().map(id -> "?").collect(Collectors.joining(","));

        String sql = FIND_TAGS_BY_LINK_IDS_TEMPLATE.formatted(inSql);

        Map<Long, Set<Tag>> result = new HashMap<>();

        jdbcTemplate.query(
                sql,
                rs -> {
                    Long linkId = rs.getLong("link_id");
                    Tag tag = tagRowMapper.mapRow(rs, 0);

                    result.computeIfAbsent(linkId, k -> new HashSet<>()).add(tag);
                },
                linkIds.toArray());

        return result;
    }

    private Map<Long, Set<Chat>> loadChats(List<Long> linkIds) {
        if (linkIds.isEmpty()) {
            return new HashMap<>();
        }

        String inSql = linkIds.stream().map(id -> "?").collect(Collectors.joining(","));

        String sql = FIND_CHATS_BY_LINK_IDS_TEMPLATE.formatted(inSql);

        Map<Long, Set<Chat>> result = new HashMap<>();

        jdbcTemplate.query(
                sql,
                rs -> {
                    Long linkId = rs.getLong("link_id");
                    Long chatId = rs.getLong("chat_id");

                    Chat chat =
                            Chat.builder().chatId(chatId).links(new HashSet<>()).build();

                    result.computeIfAbsent(linkId, k -> new HashSet<>()).add(chat);
                },
                linkIds.toArray());

        return result;
    }
}
