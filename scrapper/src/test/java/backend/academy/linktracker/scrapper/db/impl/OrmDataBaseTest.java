package backend.academy.linktracker.scrapper.db.impl;

import backend.academy.linktracker.scrapper.db.DataBaseTests;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.jpa.impl.ChatJpaAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.access-type=ORM")
class OrmDataBaseTest extends DataBaseTests {
    @Autowired
    private ChatJpaAdapter chatJpaAdapter;

    @Override
    protected ChatRepository chatRepository() {
        return chatJpaAdapter;
    }
}
