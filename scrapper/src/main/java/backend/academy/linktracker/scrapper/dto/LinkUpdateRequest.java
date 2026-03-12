package backend.academy.linktracker.scrapper.dto;

import java.util.List;

public record LinkUpdateRequest
    (
        Long id,
        String url,
        List<Long> tgChatIds
    ) {}
