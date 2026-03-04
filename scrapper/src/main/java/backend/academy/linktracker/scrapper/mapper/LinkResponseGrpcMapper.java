package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.dto.LinkResponse;

public class LinkResponseGrpcMapper {
    private LinkResponseGrpcMapper() {}

    public static LinkResponse fromProto(ScrapperProto.LinkResponse mapped) {
        return LinkResponse.builder()
            .id(mapped.getId())
            .url(mapped.getUrl())
            .tags(mapped.getTagsList())
            .filters(mapped.getFilterList())
            .build();
    }

    public static ScrapperProto.LinkResponse toProto(LinkResponse mapped) {
        return ScrapperProto.LinkResponse.newBuilder()
            .setId(mapped.getId())
            .setUrl(mapped.getUrl())
            .addAllFilter(mapped.getFilters())
            .addAllTags(mapped.getTags())
            .build();
    }
}
