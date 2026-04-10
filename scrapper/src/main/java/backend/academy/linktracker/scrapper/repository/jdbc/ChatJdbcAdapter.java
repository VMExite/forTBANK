package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.mapper.database.ChatRowMapper;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class ChatJdbcAdapter implements ChatRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ChatRowMapper chatRowMapper;

    private static final String INSERT_CHAT = """
        INSERT INTO chat(chat_id) VALUES (:chatId)
        ON CONFLICT DO NOTHING
        """;

    private static final String INSERT_LINK = """
        INSERT INTO link(url, last_update)
        VALUES (:url, :lastUpdate)
        ON CONFLICT (url) DO NOTHING
        """;

    private static final String INSERT_TAG = """
        INSERT INTO tag(name)
        VALUES (:name)
        ON CONFLICT (name) DO NOTHING
        """;

    private static final String INSERT_CHAT_LINK = """
        INSERT INTO chat_link(chat_id, link_id)
        VALUES (:chatId, :linkId)
        ON CONFLICT DO NOTHING
        """;

    private static final String INSERT_LINK_TAG = """
        INSERT INTO link_tag(link_id, tag_id)
        VALUES (:linkId, :tagId)
        ON CONFLICT DO NOTHING
        """;

    private static final String FIND_LINK_ID = """
        SELECT link_id FROM link WHERE url = :url
        """;

    private static final String FIND_TAG_ID = """
        SELECT tag_id FROM tag WHERE name = :name
        """;

    private static final String DELETE_CHAT_BY_ID = "DELETE FROM chat WHERE chat_id = :chatId";

    private static final String SELECT_CHAT_BY_ID = """
    SELECT c.chat_id,
           l.link_id,
           l.url,
           l.last_update,
           t.tag_id,
           t.name
    FROM chat c
    LEFT JOIN chat_link cl ON c.chat_id = cl.chat_id
    LEFT JOIN link l ON cl.link_id = l.link_id
    LEFT JOIN link_tag lt ON l.link_id = lt.link_id
    LEFT JOIN tag t ON lt.tag_id = t.tag_id
    WHERE c.chat_id = :chatId
    """;

    @Override
    public Optional<Chat> findById(ChatId id) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("chatId", id.value());

        return Optional.ofNullable(
            jdbcTemplate.query(SELECT_CHAT_BY_ID, params, chatRowMapper)
        );
    }

    @Override
    @Transactional
    public Chat save(Chat chat) {
        jdbcTemplate.update(INSERT_CHAT,
            new MapSqlParameterSource("chatId", chat.getChatId().value()));

        for (Link link : chat.getLinks()) {
            jdbcTemplate.update(INSERT_LINK,
                new MapSqlParameterSource()
                    .addValue("url", link.getUrl())
                    .addValue("lastUpdate", link.getLastUpdate())
            );
            Long linkId = jdbcTemplate.queryForObject(
                FIND_LINK_ID,
                new MapSqlParameterSource("url", link.getUrl()),
                Long.class
            );
            jdbcTemplate.update(INSERT_CHAT_LINK,
                new MapSqlParameterSource()
                    .addValue("chatId", chat.getChatId().value())
                    .addValue("linkId", linkId)
            );
            for (Tag tag : link.getTags()) {
                jdbcTemplate.update(INSERT_TAG,
                    new MapSqlParameterSource("name", tag.getName()));

                Long tagId = jdbcTemplate.queryForObject(
                    FIND_TAG_ID,
                    new MapSqlParameterSource("name", tag.getName()),
                    Long.class
                );
                jdbcTemplate.update(INSERT_LINK_TAG,
                    new MapSqlParameterSource()
                        .addValue("linkId", linkId)
                        .addValue("tagId", tagId)
                );
            }
        }

        return chat;
    }

    @Override
    @Transactional
    public void deleteById(ChatId id) {
        jdbcTemplate.update(
            DELETE_CHAT_BY_ID,
            new MapSqlParameterSource("chatId", id.value())
        );
    }
}
