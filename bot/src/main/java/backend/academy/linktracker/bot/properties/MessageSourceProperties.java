package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.message")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class MessageSourceProperties {
    @NotEmpty
    private String basename;
}
