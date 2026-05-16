package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.cache")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class RedisCacheProperties {
    @Min(1)
    @Max(10_000)
    private Integer ttl;
}
