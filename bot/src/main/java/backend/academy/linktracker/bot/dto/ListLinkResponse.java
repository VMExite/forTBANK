package backend.academy.linktracker.bot.dto;

import java.util.List;

public record ListLinkResponse(
    List<LinkResponse> links,
    Integer size
) {}
