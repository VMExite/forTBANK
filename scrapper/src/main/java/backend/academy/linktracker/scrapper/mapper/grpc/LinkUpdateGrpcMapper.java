package backend.academy.linktracker.scrapper.mapper.grpc;

import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import java.util.ArrayList;
// Todo: grpc usless now

public class LinkUpdateGrpcMapper {
    private LinkUpdateGrpcMapper() {}

    public static ScrapperProto.LinkUpdate toProto(LinkUpdateMessage mapped) {
        return ScrapperProto.LinkUpdate.newBuilder()
                .setId(mapped.linkId())
                .setUrl(mapped.url())
                .setDescription("")
                .addAllTgChatIds(new ArrayList<>())
                .build();
    }
}
