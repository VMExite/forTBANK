package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.dto.LinkResponse;

public class LinkResponseGrpcMapper {
    private LinkResponseGrpcMapper() {}

    public static LinkResponse fromProto(ScrapperProto.LinkResponse mapped) {
        return new LinkResponse(mapped.getId(), mapped.getUrl(), mapped.getTagsList(), mapped.getFiltersList());
    }

    public static ScrapperProto.LinkResponse toProto(LinkResponse mapped) {
        return ScrapperProto.LinkResponse.newBuilder()
                .setId(mapped.id())
                .setUrl(mapped.url())
                .addAllTags(mapped.tags())
                .addAllFilters(mapped.filters())
                .build();
    }
}
