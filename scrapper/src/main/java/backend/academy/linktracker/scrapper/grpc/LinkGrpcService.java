package backend.academy.linktracker.scrapper.grpc;

import backend.academy.linktracker.scrapper.LinkServiceGrpc;
import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinkResponse;
import backend.academy.linktracker.scrapper.mapper.AddLinkRequestGrpcMapper;
import backend.academy.linktracker.scrapper.mapper.LinkResponseGrpcMapper;
import backend.academy.linktracker.scrapper.mapper.ListLinkResponseGrpcMapper;
import backend.academy.linktracker.scrapper.mapper.RemoveLinkRequestGrpcMapper;
import backend.academy.linktracker.scrapper.service.LinksService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class LinkGrpcService extends LinkServiceGrpc.LinkServiceImplBase {
    private final LinksService linksService;

    @Override
    public void getLinks(ScrapperProto.GetLinkRequest request,
                         StreamObserver<ScrapperProto.ListLinkResponse> responseObserver) {
        ListLinkResponse response = linksService.getLinks(request.getTgChatId());
        ScrapperProto.ListLinkResponse grpcResponse = ListLinkResponseGrpcMapper.toProto(response);

        responseObserver.onNext(grpcResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void createLink(ScrapperProto.AddLinkRequest request,
                           StreamObserver<ScrapperProto.LinkResponse> responseObserver) {
        LinkResponse response = linksService.createLink(
            request.getTgChatId(),
            AddLinkRequestGrpcMapper.fromProto(request)
        );
        ScrapperProto.LinkResponse grpcResponse = LinkResponseGrpcMapper.toProto(response);
        responseObserver.onNext(grpcResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteLink(ScrapperProto.RemoveLinkRequest request,
                           StreamObserver<ScrapperProto.LinkResponse> responseObserver) {
        LinkResponse response = linksService.removeLink(
            request.getTgChaId(),
            RemoveLinkRequestGrpcMapper.fromProto(request)
        );
        ScrapperProto.LinkResponse grpcResponse = LinkResponseGrpcMapper.toProto(response);
        responseObserver.onNext(grpcResponse);
        responseObserver.onCompleted();
    }
}
