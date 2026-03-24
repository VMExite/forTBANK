package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.bot.grpc")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class BotGrpcProperties {
    @NotEmpty
    private String host;

    @NotNull
    private Integer port;
}
