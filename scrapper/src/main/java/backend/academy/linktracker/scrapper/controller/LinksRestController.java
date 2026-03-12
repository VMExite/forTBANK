package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinkResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.service.LinksService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
@Validated
public class LinksRestController {
    private final LinksService linksService;

    @GetMapping
    public ResponseEntity<ListLinkResponse> getLinks(
        @RequestHeader(name = "Tg-Chat-Id") @NotNull @Parameter Long tgChatId
    ) {
        ListLinkResponse response = linksService.getLinks(tgChatId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<LinkResponse> createLink(
        @RequestHeader(name = "Tg-Chat-Id") @NotNull @Parameter Long tgChatId,
        @Valid @RequestBody AddLinkRequest request
    ) {
        LinkResponse response = linksService.createLink(tgChatId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> deleteLink(
        @RequestHeader(name = "Tg-Chat-Id") @NotNull @Parameter Long tgChatId,
        @Valid @RequestBody RemoveLinkRequest request
    ) {
        LinkResponse response = linksService.removeLink(tgChatId, request);
        return ResponseEntity.ok(response);
    }
}
