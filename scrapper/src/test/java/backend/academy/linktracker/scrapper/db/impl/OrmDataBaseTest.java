package backend.academy.linktracker.scrapper.db.impl;

import backend.academy.linktracker.scrapper.db.DataBaseTests;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class OrmDataBaseTest extends DataBaseTests {
    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("app.access-type", () -> "ORM");
    }
}
