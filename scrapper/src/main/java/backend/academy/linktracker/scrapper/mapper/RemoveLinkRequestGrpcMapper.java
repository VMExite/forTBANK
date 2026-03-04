package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;

public class RemoveLinkRequestGrpcMapper {
    private RemoveLinkRequestGrpcMapper() {}

    public static RemoveLinkRequest fromProto(ScrapperProto.RemoveLinkRequest mapped) {
        return new RemoveLinkRequest(mapped.getLink());
    }

    public static ScrapperProto.RemoveLinkRequest toProto(Long tgChatId, RemoveLinkRequest mapped) {
        return ScrapperProto.RemoveLinkRequest.newBuilder()
            .setTgChaId(tgChatId)
            .setLink(mapped.link())
            .build();
    }
}
