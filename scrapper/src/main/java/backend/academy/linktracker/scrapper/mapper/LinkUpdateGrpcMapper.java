package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;

public class LinkUpdateGrpcMapper {
    private LinkUpdateGrpcMapper() {}

    public static ScrapperProto.LinkUpdate toProto(LinkUpdateRequest mapped) {
        return ScrapperProto.LinkUpdate.newBuilder()
            .setId(mapped.id())
            .setUrl(mapped.url())
            .setDescription("")
            .addAllTgChatIds(mapped.tgChatIds())
            .build();
    }
}
