package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.properties.LiquibaseProperties;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LiquibaseConfig {
    private final LiquibaseProperties properties;

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(dataSource);
        springLiquibase.setChangeLog(properties.getChangeLog());
        springLiquibase.setShouldRun(properties.isEnabled());
        return springLiquibase;
    }
}
