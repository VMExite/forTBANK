package backend.academy.linktracker.scrapper.advice;

import backend.academy.linktracker.scrapper.dto.ApiErrorResponse;
import backend.academy.linktracker.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyTracked;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.Arrays;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ChatAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleChatAlreadyExists(ChatAlreadyExistsException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(buildBody("Чат уже существует", HttpStatus.CONFLICT, ex));
    }

    @ExceptionHandler(ChatNotExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleChatNotExists(ChatNotExistsException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(buildBody("Чат не существует", HttpStatus.NOT_FOUND, ex));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(buildBody("Некорректные параметры запроса", HttpStatus.BAD_REQUEST, ex));
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLinkNotFound(LinkNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(buildBody("Ссылка не найдена", HttpStatus.NOT_FOUND, ex));
    }

    @ExceptionHandler(LinkAlreadyTracked.class)
    public ResponseEntity<ApiErrorResponse> handleLinkAlreadyExists(LinkAlreadyTracked ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(buildBody("Ссылка уже отслеживается", HttpStatus.CONFLICT, ex));
    }


    private ApiErrorResponse buildBody(String description, HttpStatus status, Exception ex) {
        return new ApiErrorResponse(
            description,
            String.valueOf(status.value()),
            ex.getClass().getName(),
            ex.getMessage(),
            Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .toList()
        );
    }
}
