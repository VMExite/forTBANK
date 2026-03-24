package backend.academy.linktracker.scrapper.mapper.grpc;

import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.dto.ListLinkResponse;

public class ListLinkResponseGrpcMapper {
    private ListLinkResponseGrpcMapper() {}

    public static ListLinkResponse fromProto(ScrapperProto.ListLinkResponse mapped) {
        return new ListLinkResponse(
                mapped.getLinksList().stream()
                        .map(LinkResponseGrpcMapper::fromProto)
                        .toList(),
                mapped.getSize());
    }

    public static ScrapperProto.ListLinkResponse toProto(ListLinkResponse mapped) {
        return ScrapperProto.ListLinkResponse.newBuilder()
                .addAllLinks(mapped.links().stream()
                        .map(LinkResponseGrpcMapper::toProto)
                        .toList())
                .setSize(mapped.size())
                .build();
    }
}
