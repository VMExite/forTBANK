package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.dto.AddLinkRequest;

public class AddLinkRequestGrpcMapper {
    private AddLinkRequestGrpcMapper() {}

    public static AddLinkRequest fromProto(ScrapperProto.AddLinkRequest mapped) {
        return AddLinkRequest.builder()
            .link(mapped.getLink())
            .tags(mapped.getTagsList())
            .filters(mapped.getFilterList())
            .build();
    }

    public static ScrapperProto.AddLinkRequest toProto(Long tgChatId, AddLinkRequest mapped) {
        return ScrapperProto.AddLinkRequest.newBuilder()
            .setTgChatId(tgChatId)
            .setLink(mapped.getLink())
            .addAllTags(mapped.getTags())
            .addAllFilter(mapped.getFilters())
            .build();
    }
}
