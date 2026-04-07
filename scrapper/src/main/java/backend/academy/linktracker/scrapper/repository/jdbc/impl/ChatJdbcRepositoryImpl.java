package backend.academy.linktracker.scrapper.repository.jdbc.impl;

import backend.academy.linktracker.scrapper.mapper.database.ChatRowMapper;
import backend.academy.linktracker.scrapper.mapper.database.LinkRowMapper;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.jdbc.ChatJdbcRepository;
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
public class ChatJdbcRepositoryImpl implements ChatJdbcRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ChatRowMapper chatRowMapper;
    private final LinkRowMapper linkRowMapper;

    private static final String FIND_ALL_SQL = """
        SELECT chat_id
        FROM chat
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT chat_id
        FROM chat
        WHERE chat_id = :id
        """;

    private static final String INSERT_SQL = """
        INSERT INTO chat (chat_id)
        VALUES (:id)
        ON CONFLICT (chat_id) DO NOTHING
        """;

    private static final String DELETE_SQL = """
        DELETE FROM chat
        WHERE chat_id = :id
        """;

    private static final String EXISTS_SQL = """
        SELECT EXISTS (
            SELECT 1 FROM chat WHERE chat_id = :id
        )
        """;

    private static final String FIND_LINKS_BY_CHAT_IDS_SQL = """
        SELECT cl.chat_id, l.link_id, l.url, l.last_update
        FROM chat_link cl
        JOIN link l ON l.link_id = cl.link_id
        WHERE cl.chat_id IN (:ids)
        """;

    private static final String INSERT_CHAT_LINK_SQL = """
        INSERT INTO chat_link (chat_id, link_id)
        VALUES (:chatId, :linkId)
        ON CONFLICT DO NOTHING
        """;

    private static final String DELETE_CHAT_LINK_SQL = """
        DELETE FROM chat_link
        WHERE chat_id = :chatId AND link_id = :linkId
        """;

    private static final String DELETE_ALL_CHAT_LINKS_SQL = """
        DELETE FROM chat_link
        WHERE chat_id = :id
        """;

    private static final String SELECT_LINK_IDS_SQL = """
        SELECT link_id FROM chat_link WHERE chat_id = :id
        """;

    @Override
    public List<Chat> findAll() {
        List<Chat> chats = jdbcTemplate.getJdbcTemplate().query(FIND_ALL_SQL, chatRowMapper);
        return enrichChats(chats);
    }

    @Override
    public Optional<Chat> findById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);

        Optional<Chat> chat = jdbcTemplate.query(FIND_BY_ID_SQL, params, chatRowMapper).stream()
                .findFirst();

        chat.ifPresent(this::enrichChat);
        return chat;
    }

    @Override
    public boolean existsById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        Boolean exists = jdbcTemplate.queryForObject(EXISTS_SQL, params, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    @Transactional
    public Chat save(Chat entity) {
        Long chatId = entity.getChatId();

        MapSqlParameterSource params = new MapSqlParameterSource("id", chatId);
        jdbcTemplate.update(INSERT_SQL, params);

        syncLinks(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Chat entity) {
        deleteById(entity.getChatId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(DELETE_ALL_CHAT_LINKS_SQL, params);
        jdbcTemplate.update(DELETE_SQL, params);
    }

    private void syncLinks(Chat chat) {
        Long chatId = chat.getChatId();

        // existing links
        MapSqlParameterSource params = new MapSqlParameterSource("id", chatId);

        List<Long> existingList =
                jdbcTemplate.query(SELECT_LINK_IDS_SQL, params, (rs, rowNum) -> rs.getLong("link_id"));

        Set<Long> existing = new HashSet<>(existingList);

        Set<Long> incoming = chat.getLinks().stream()
                .map(Link::getLinkId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> toInsert = new HashSet<>(incoming);
        toInsert.removeAll(existing);

        Set<Long> toDelete = new HashSet<>(existing);
        toDelete.removeAll(incoming);

        batchInsert(chatId, toInsert);
        batchDelete(chatId, toDelete);
    }

    private void batchInsert(Long chatId, Set<Long> linkIds) {
        if (linkIds.isEmpty()) return;

        List<MapSqlParameterSource> batch = linkIds.stream()
                .map(linkId ->
                        new MapSqlParameterSource().addValue("chatId", chatId).addValue("linkId", linkId))
                .toList();

        jdbcTemplate.batchUpdate(INSERT_CHAT_LINK_SQL, batch.toArray(new MapSqlParameterSource[0]));
    }

    private void batchDelete(Long chatId, Set<Long> linkIds) {
        if (linkIds.isEmpty()) return;

        List<MapSqlParameterSource> batch = linkIds.stream()
                .map(linkId ->
                        new MapSqlParameterSource().addValue("chatId", chatId).addValue("linkId", linkId))
                .toList();

        jdbcTemplate.batchUpdate(DELETE_CHAT_LINK_SQL, batch.toArray(new MapSqlParameterSource[0]));
    }

    private List<Chat> enrichChats(List<Chat> chats) {
        if (chats.isEmpty()) return chats;

        List<Long> chatIds = chats.stream().map(Chat::getChatId).toList();
        Map<Long, Set<Link>> linksMap = loadLinks(chatIds);

        for (Chat chat : chats) {
            chat.setLinks(new HashSet<>(linksMap.getOrDefault(chat.getChatId(), Collections.emptySet())));
        }

        return chats;
    }

    private void enrichChat(Chat chat) {
        Map<Long, Set<Link>> links = loadLinks(List.of(chat.getChatId()));
        chat.setLinks(new HashSet<>(links.getOrDefault(chat.getChatId(), Collections.emptySet())));
    }

    private Map<Long, Set<Link>> loadLinks(List<Long> chatIds) {
        if (chatIds.isEmpty()) return new HashMap<>();

        MapSqlParameterSource params = new MapSqlParameterSource("ids", chatIds);

        Map<Long, Set<Link>> result = new HashMap<>();

        jdbcTemplate.query(FIND_LINKS_BY_CHAT_IDS_SQL, params, rs -> {
            Long chatId = rs.getLong("chat_id");
            Link link = linkRowMapper.mapRow(rs, 0);

            result.computeIfAbsent(chatId, k -> new HashSet<>()).add(link);
        });

        return result;
    }
}
