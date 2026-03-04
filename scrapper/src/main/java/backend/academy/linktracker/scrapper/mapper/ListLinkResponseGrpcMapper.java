package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.dto.ListLinkResponse;

public class ListLinkResponseGrpcMapper {
    private ListLinkResponseGrpcMapper() {}

    public static ListLinkResponse fromProto(ScrapperProto.ListLinkResponse mapped) {
        return ListLinkResponse.builder()
            .links(
                mapped.getLinksList()
                    .stream()
                    .map(LinkResponseGrpcMapper::fromProto)
                    .toList()
            )
            .size(mapped.getSize())
            .build();
    }

    public static ScrapperProto.ListLinkResponse toProto(ListLinkResponse mapped) {
        return ScrapperProto.ListLinkResponse.newBuilder()
            .addAllLinks(
                mapped.getLinks()
                    .stream()
                    .map(LinkResponseGrpcMapper::toProto)
                    .toList()
            )
            .setSize(mapped.getSize())
            .build();
    }
}
