package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;

public class RemoveLinkRequestGrpcMapper {
    private RemoveLinkRequestGrpcMapper() {}

    public static RemoveLinkRequest fromProto(ScrapperProto.RemoveLinkRequest mapped) {
        return RemoveLinkRequest.builder()
            .link(mapped.getLink())
            .build();
    }

    public static ScrapperProto.RemoveLinkRequest toProto(Long tgChatId, RemoveLinkRequest mapped) {
        return ScrapperProto.RemoveLinkRequest.newBuilder()
            .setTgChaId(tgChatId)
            .setLink(mapped.getLink())
            .build();
    }
}
