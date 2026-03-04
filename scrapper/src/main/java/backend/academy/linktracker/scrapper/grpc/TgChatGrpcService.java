package backend.academy.linktracker.scrapper.grpc;

import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.TgChatServiceGrpc;
import backend.academy.linktracker.scrapper.service.RegistrationService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class TgChatGrpcService extends TgChatServiceGrpc.TgChatServiceImplBase {
    private final RegistrationService registrationService;


    @Override
    public void deleteChat(ScrapperProto.DeleteChatRequest request,
                           StreamObserver<ScrapperProto.ChatResponse> responseObserver) {
        registrationService.deleteChat(request.getId());
        responseObserver.onNext(
            ScrapperProto.ChatResponse.newBuilder()
                .setMessage("чат удален")
                .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void registerChat(ScrapperProto.RegisterChatRequest request,
                             StreamObserver<ScrapperProto.ChatResponse> responseObserver) {
        registrationService.registerChat(request.getId());
        responseObserver.onNext(
            ScrapperProto.ChatResponse.newBuilder()
                .setMessage("чат добавлен")
                .build()
        );
        responseObserver.onCompleted();
    }
}
