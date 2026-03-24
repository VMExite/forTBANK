package backend.academy.linktracker.scrapper.repository.jdbc.impl;

import backend.academy.linktracker.scrapper.mapper.database.ChatRowMapper;
import backend.academy.linktracker.scrapper.mapper.database.LinkRowMapper;
import backend.academy.linktracker.scrapper.mapper.database.TagRowMapper;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.jdbc.ChatJdbcRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
@RequiredArgsConstructor
public class ChatJdbcRepositoryImpl implements ChatJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ChatRowMapper chatRowMapper;
    private final LinkRowMapper linkRowMapper;
    private final TagRowMapper tagRowMapper;

    private static final String FIND_ALL_SQL = """
        SELECT chat_id
        FROM chat
        """;
    private static final String FIND_BY_ID_SQL = """
        SELECT chat_id
        FROM chat
        WHERE chat_id = ?
        """;

    private static final String INSERT_CHAT_SQL = """
        INSERT INTO chat (chat_id)
        VALUES (?)
        ON CONFLICT (chat_id) DO NOTHING
        """;

    private static final String UPDATE_CHAT_SQL = """
        UPDATE chat
        SET chat_id = ?
        WHERE chat_id = ?
        """;

    private static final String INSERT_LINK_SQL = """
        INSERT INTO link (url, last_update)
        VALUES (?, ?)
        RETURNING link_id
        """;

    private static final String INSERT_CHAT_LINK_SQL = """
        INSERT INTO chat_link (chat_id, link_id)
        VALUES (?, ?)
        ON CONFLICT DO NOTHING
        """;
    private static final String INSERT_TAG_SQL = """
        INSERT INTO tag (name)
        VALUES (?)
        RETURNING tag_id
        """;
    private static final String INSERT_LINK_TAG_SQL = """
        INSERT INTO link_tag (link_id, tag_id)
        VALUES (?, ?)
        """;
    private static final String FIND_LINKS_BY_CHAT_ID_SQL = """
        SELECT l.link_id, l.url, l.last_update
        FROM link l
        JOIN chat_link cl ON cl.link_id = l.link_id
        WHERE cl.chat_id = ?
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

    private static final String DELETE_SQL = """
        DELETE FROM chat
        WHERE chat_id = ?
        """;
    private static final String COUNT_BY_ID_SQL = """
        SELECT COUNT(*)
        FROM chat
        WHERE chat_id = ?
        """;

    @Override
    public List<Chat> findAll() {
        List<Chat> chats = jdbcTemplate.query(FIND_ALL_SQL, chatRowMapper);
        for (Chat chat : chats) {
            populateLinks(chat);
        }
        return chats;
    }

    @Override
    public Optional<Chat> findById(Long id) {
        Optional<Chat> chat =
                jdbcTemplate.query(FIND_BY_ID_SQL, chatRowMapper, id).stream().findFirst();
        chat.ifPresent(this::populateLinks);
        return chat;
    }

    @Override
    @Transactional
    public Chat save(Chat entity) {
        int inserted = jdbcTemplate.update(INSERT_CHAT_SQL, entity.getChatId());
        if (inserted == 0) {
            jdbcTemplate.update(UPDATE_CHAT_SQL, entity.getChatId(), entity.getChatId());
        }
        for (Link link : entity.getLinks()) {
            Long linkId = link.getLinkId();
            if (linkId == null) {
                linkId = insertLinkAndGetId(link);
                link.setLinkId(linkId);
            }

            jdbcTemplate.update(INSERT_CHAT_LINK_SQL, entity.getChatId(), linkId);
            if (link.getTags() != null) {
                for (Tag tag : link.getTags()) {
                    Long tagId = tag.getTagId();
                    if (tagId == null) {
                        tagId = insertTagAndGetId(tag);
                        tag.setTagId(tagId);
                    }
                    jdbcTemplate.update(INSERT_LINK_TAG_SQL, linkId, tagId);
                }
            }
        }

        return entity;
    }

    private Long insertLinkAndGetId(Link link) {
        return jdbcTemplate.queryForObject(INSERT_LINK_SQL, Long.class, link.getUrl(), link.getLastUpdate());
    }

    private Long insertTagAndGetId(Tag tag) {
        return jdbcTemplate.queryForObject(INSERT_TAG_SQL, Long.class, tag.getName());
    }

    private void populateLinks(Chat chat) {
        List<Link> links = jdbcTemplate.query(FIND_LINKS_BY_CHAT_ID_SQL, linkRowMapper, chat.getChatId());
        for (Link link : links) {
            enrichLink(link);
        }
        chat.getLinks().clear();
        chat.getLinks().addAll(links);
    }

    private void enrichLink(Link link) {
        List<Tag> tags = jdbcTemplate.query(FIND_TAGS_BY_LINK_ID_SQL, tagRowMapper, link.getLinkId());
        List<Chat> chats = jdbcTemplate.query(FIND_CHATS_BY_LINK_ID_SQL, chatRowMapper, link.getLinkId());
        link.setTags(tags);
        link.setChats(chats);
    }

    @Override
    public void delete(Chat entity) {
        deleteById(entity.getChatId());
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(COUNT_BY_ID_SQL, Integer.class, id);
        return count != null && count > 0;
    }
}
