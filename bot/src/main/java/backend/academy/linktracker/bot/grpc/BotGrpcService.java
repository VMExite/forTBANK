package backend.academy.linktracker.bot.grpc;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.UpdateNotificationService;
import backend.academy.linktracker.scrapper.BotServiceGrpc;
import backend.academy.linktracker.scrapper.ScrapperProto;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class BotGrpcService extends BotServiceGrpc.BotServiceImplBase {
    private final UpdateNotificationService updateNotificationService;

    @Override
    public void sendUpdate(
            ScrapperProto.LinkUpdate request, StreamObserver<ScrapperProto.BotResponse> responseObserver) {
        LinkUpdate update =
                new LinkUpdate(request.getId(), request.getUrl(), request.getDescription(), request.getTgChatIdsList());
        updateNotificationService.notifyUsers(update);

        responseObserver.onNext(
                ScrapperProto.BotResponse.newBuilder().setMessage("ok").build());
        responseObserver.onCompleted();
    }
}
