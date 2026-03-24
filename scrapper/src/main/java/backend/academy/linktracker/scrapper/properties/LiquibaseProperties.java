package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "spring.liquibase")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class LiquibaseProperties {
    private boolean enabled;

    @NotEmpty
    private String changeLog;
}
