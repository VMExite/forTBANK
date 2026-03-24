package backend.academy.linktracker.scrapper.repository.jdbc.impl;

import backend.academy.linktracker.scrapper.mapper.database.ChatRowMapper;
import backend.academy.linktracker.scrapper.mapper.database.LinkRowMapper;
import backend.academy.linktracker.scrapper.mapper.database.TagRowMapper;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.jdbc.LinkJdbcRepository;
import java.util.List;
import java.util.Optional;
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
    private static final String FIND_TAGS_BY_LINK_ID_SQL = """
        SELECT t.tag_id, t.name
        FROM tag t
        JOIN link_tag lt ON lt.tag_id = t.tag_id
        WHERE lt.link_id = ?
        """;
    private static final String FIND_CHATS_BY_LINK_ID_SQL = """
        SELECT c.chat_id
        FROM chat c
        JOIN chat_link cl ON cl.chat_id = c.chat_id
        WHERE cl.link_id = ?
        """;

    @Override
    public List<Link> findLinkByUrl(String url) {
        List<Link> links = jdbcTemplate.query(FIND_BY_URL_SQL, linkRowMapper, url);
        return enrichLinks(links);
    }

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
    public Link save(Link entity) {
        if (entity.getLinkId() == null) {
            Long id = jdbcTemplate.queryForObject(INSERT_SQL, Long.class, entity.getUrl(), entity.getLastUpdate());
            entity.setLinkId(id);
        } else {
            jdbcTemplate.update(UPDATE_SQL, entity.getUrl(), entity.getLastUpdate(), entity.getLinkId());
        }
        return entity;
    }

    @Override
    public void delete(Link entity) {
        jdbcTemplate.update(DELETE_SQL, entity.getLinkId());
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }

    private List<Link> enrichLinks(List<Link> links) {
        for (Link link : links) {
            enrichLink(link);
        }
        return links;
    }

    private void enrichLink(Link link) {
        List<Tag> tags = jdbcTemplate.query(FIND_TAGS_BY_LINK_ID_SQL, tagRowMapper, link.getLinkId());
        List<Chat> chats = jdbcTemplate.query(FIND_CHATS_BY_LINK_ID_SQL, chatRowMapper, link.getLinkId());
        link.setTags(tags);
        link.setChats(chats);
    }
}
