package backend.academy.linktracker.scrapper.mapper.database;

import backend.academy.linktracker.scrapper.model.Chat;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class ChatRowMapper implements RowMapper<Chat> {
    @Override
    public Chat mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Chat.builder()
                .chatId(rs.getLong("chat_id"))
                .links(new HashSet<>())
                .build();
    }
}
