package backend.academy.linktracker.bot.dto;

import java.time.OffsetDateTime;

public record LinkUpdateMessage(
        Long eventId,
        Long chatId,
        Long linkId,
        String title,
        String username,
        String preview,
        String url,
        OffsetDateTime createdAt) {}
