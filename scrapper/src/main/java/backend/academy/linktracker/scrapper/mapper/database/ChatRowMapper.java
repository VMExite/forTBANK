package backend.academy.linktracker.scrapper.mapper.database;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import backend.academy.linktracker.scrapper.model.value.LinkId;
import backend.academy.linktracker.scrapper.model.value.TagId;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public final class ChatRowMapper implements ResultSetExtractor<Chat> {

    @Override
    public Chat extractData(ResultSet rs) throws SQLException {
        Chat chat = null;
        Map<Long, Link> links = new HashMap<>();
        Map<Long, Set<Tag>> linkTags = new HashMap<>();

        while (rs.next()) {
            if (chat == null) {
                chat = Chat.builder().chatId(new ChatId(rs.getLong("chat_id"))).build();
            }
            Long linkId = rs.getLong("link_id");
            if (!rs.wasNull()) {
                Link link = links.get(linkId);
                if (link == null) {
                    link = Link.builder()
                            .linkId(new LinkId(linkId))
                            .url(getStringSafe(rs, "url"))
                            .lastUpdate(rs.getObject("last_update", OffsetDateTime.class))
                            .build();

                    links.put(linkId, link);
                }
                Long tagId = rs.getLong("tag_id");
                if (!rs.wasNull()) {
                    Tag tag = Tag.builder()
                            .tagId(new TagId(tagId))
                            .name(getStringSafe(rs, "name"))
                            .build();
                    linkTags.computeIfAbsent(linkId, k -> new HashSet<>()).add(tag);
                }
            }
        }
        if (chat == null) {
            return null;
        }
        for (Link link : links.values()) {
            Set<Tag> tags = linkTags.getOrDefault(link.getLinkId().value(), Set.of());
            for (Tag tag : tags) {
                link.addTag(tag);
            }
            chat.addLink(link);
        }

        return chat;
    }

    private String getStringSafe(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return rs.wasNull() ? null : value;
    }
}
