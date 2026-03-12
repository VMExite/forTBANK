package backend.academy.linktracker.scrapper.advice;

import backend.academy.linktracker.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyTracked;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GlobalGrpcExceptionHandler {
    @GrpcExceptionHandler(IllegalArgumentException.class)
    public Status handleInvalidArgument(IllegalArgumentException ex) {
        return Status.INVALID_ARGUMENT
                .withDescription("некорректные параметры запроса")
                .withCause(ex);
    }

    @GrpcExceptionHandler(ChatAlreadyExistsException.class)
    public Status handleChatAlreadyExists(ChatAlreadyExistsException ex) {
        return Status.ALREADY_EXISTS.withDescription("чат уже существует");
    }

    @GrpcExceptionHandler(ChatNotExistsException.class)
    public Status handleChatNotExists(ChatNotExistsException ex) {
        return Status.NOT_FOUND.withDescription("чат не существует").withCause(ex);
    }

    @GrpcExceptionHandler(LinkAlreadyTracked.class)
    public Status handleLinkAlreadyTracked(LinkAlreadyTracked ex) {
        return Status.ALREADY_EXISTS.withDescription("ссылка уже отслеживается").withCause(ex);
    }

    @GrpcExceptionHandler(LinkNotFoundException.class)
    public Status handleLinkNotFound(LinkNotFoundException ex) {
        return Status.NOT_FOUND.withDescription("ссылка не найдена").withCause(ex);
    }
}
