package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.dto.AddLinkRequest;

public class AddLinkRequestGrpcMapper {
    private AddLinkRequestGrpcMapper() {}

    public static AddLinkRequest fromProto(ScrapperProto.AddLinkRequest mapped) {
        return new AddLinkRequest(
            mapped.getLink(),
            mapped.getTagsList(),
            mapped.getFilterList()
        );
    }

    public static ScrapperProto.AddLinkRequest toProto(Long tgChatId, AddLinkRequest mapped) {
        return ScrapperProto.AddLinkRequest.newBuilder()
            .setTgChatId(tgChatId)
            .setLink(mapped.link())
            .addAllTags(mapped.tags())
            .addAllFilter(mapped.filters())
            .build();
    }
}
