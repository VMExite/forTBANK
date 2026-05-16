package backend.academy.linktracker.scrapper.db.impl;

import backend.academy.linktracker.scrapper.db.DataBaseTests;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.ChatJdbcAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.access-type=SQL")
class SqlDataBaseTest extends DataBaseTests {
    @Autowired
    private ChatJdbcAdapter chatJdbcAdapter;

    @Override
    protected ChatRepository chatRepository() {
        return chatJdbcAdapter;
    }
}
