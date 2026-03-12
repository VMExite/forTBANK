package backend.academy.linktracker.bot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.hibernate.validator.constraints.URL;

public record LinkUpdate(
        @NotNull Long id,
        @NotBlank @URL String url,
        String description,
        @NotNull List<@NotNull Long> tgChatIds) {}
