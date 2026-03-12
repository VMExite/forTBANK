package backend.academy.linktracker.scrapper.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record AddLinkRequest(
    @NotBlank @URL String link,
    List<String> tags,
    List<String> filters
) {}
