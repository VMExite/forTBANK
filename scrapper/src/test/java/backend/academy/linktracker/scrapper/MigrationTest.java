package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class MigrationTest {
    private static final String SQL = """
    SELECT count(*)
    FROM information_schema.tables
    WHERE table_schema='public' AND table_name=?
    """;

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")
            .withUrlParam("ssl", "false");

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void liquibaseAppliesSchema() {
        List<String> expectedTables = List.of("chat", "link", "tag", "chat_link", "link_tag", "databasechangelog");

        for (String table : expectedTables) {
            Integer count = jdbcTemplate.queryForObject(SQL, Integer.class, table);
            assertThat(count).as("table %s exists", table).isEqualTo(1);
        }
    }
}
