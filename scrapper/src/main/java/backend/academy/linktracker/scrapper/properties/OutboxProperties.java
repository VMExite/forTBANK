package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.outbox")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class OutboxProperties {

    @Min(1)
    @Max(500)
    private Integer batchSize;

    @Min(1)
    @Max(10)
    private Integer maxRetry;

    @Min(1)
    @Max(10)
    private Integer threads;

    @NotNull
    @Positive
    private Integer terminationAwaitMillis;
}
