package backend.academy.linktracker.scrapper.webclient.bot;

import backend.academy.linktracker.scrapper.BotServiceGrpc;
import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.mapper.LinkUpdateGrpcMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BotGrpcClient {
    private final BotServiceGrpc.BotServiceBlockingStub client;

    public void sendUpdate(LinkUpdateRequest request) {
        ScrapperProto.LinkUpdate update = LinkUpdateGrpcMapper.toProto(request);
        client.sendUpdate(update);
    }
}
