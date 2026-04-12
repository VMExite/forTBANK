package backend.academy.linktracker.scrapper.dto;

import java.time.OffsetDateTime;

public record LinkUpdateMessage(
        Long chatId,
        Long linkId,
        String title,
        String username,
        String preview,
        String url,
        OffsetDateTime createdAt) {}
