package backend.academy.linktracker.scrapper.mapper.grpc;

import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;

public class LinkUpdateGrpcMapper {
    private LinkUpdateGrpcMapper() {}

    public static ScrapperProto.LinkUpdate toProto(LinkUpdateMessage mapped) {
        return ScrapperProto.LinkUpdate.newBuilder()
                .setId(mapped.id())
                .setUrl(mapped.url())
                .setDescription("")
                .addAllTgChatIds(mapped.tgChatIds())
                .build();
    }
}
