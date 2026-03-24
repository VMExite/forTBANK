package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.properties.DatabaseProperties;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
@RequiredArgsConstructor
public class DatabaseConfig {
    private final DatabaseProperties properties;

    @Bean
    public DataSource dataSource() {
        return new DriverManagerDataSource(properties.getUrl(), properties.getUsername(), properties.getPassword());
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
