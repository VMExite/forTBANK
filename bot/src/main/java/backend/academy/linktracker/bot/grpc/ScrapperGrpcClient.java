package backend.academy.linktracker.bot.grpc;

import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinkResponse;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.LinkServiceGrpc;
import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.TgChatServiceGrpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScrapperGrpcClient {
    private final LinkServiceGrpc.LinkServiceBlockingStub linkClient;
    private final TgChatServiceGrpc.TgChatServiceBlockingStub chatClient;

    public void registerChat(long chatId) {
        chatClient.registerChat(
                ScrapperProto.RegisterChatRequest.newBuilder().setId(chatId).build());
    }

    public void deleteChat(long chatId) {
        chatClient.deleteChat(
                ScrapperProto.DeleteChatRequest.newBuilder().setId(chatId).build());
    }

    public ListLinkResponse getLinks(long chatId) {
        ScrapperProto.ListLinkResponse response = linkClient.getLinks(
                ScrapperProto.GetLinkRequest.newBuilder().setTgChatId(chatId).build());
        return new ListLinkResponse(
                response.getLinksList().stream().map(this::mapLink).toList(), response.getSize());
    }

    public LinkResponse addLink(long chatId, AddLinkRequest request) {
        ScrapperProto.LinkResponse response = linkClient.createLink(ScrapperProto.AddLinkRequest.newBuilder()
                .setTgChatId(chatId)
                .setLink(request.link())
                .addAllTags(request.tags())
                .addAllFilters(request.filters())
                .build());
        return mapLink(response);
    }

    public LinkResponse removeLink(long chatId, RemoveLinkRequest request) {
        ScrapperProto.LinkResponse response = linkClient.deleteLink(ScrapperProto.RemoveLinkRequest.newBuilder()
                .setTgChatId(chatId)
                .setLink(request.link())
                .build());
        return mapLink(response);
    }

    private LinkResponse mapLink(ScrapperProto.LinkResponse response) {
        return new LinkResponse(response.getId(), response.getUrl(), response.getTagsList(), response.getFiltersList());
    }
}
