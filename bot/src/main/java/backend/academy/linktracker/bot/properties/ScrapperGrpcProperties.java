package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.scrapper.grpc")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class ScrapperGrpcProperties {
    @NotNull
    private String host;

    @NotNull
    private Integer port;
}
