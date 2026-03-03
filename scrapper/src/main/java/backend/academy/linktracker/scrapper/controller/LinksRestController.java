package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinkResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.service.LinksService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinksRestController {
    private final LinksService linksService;

    @GetMapping
    public ResponseEntity<ListLinkResponse> getLinks(@Parameter Long tgChatId) {
        ListLinkResponse response = linksService.getLinks(tgChatId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<LinkResponse> createLink(@Parameter Long tgChatId, @RequestBody AddLinkRequest request) {
        LinkResponse response = linksService.createLink(tgChatId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> deleteLink(@Parameter Long tgChatId, @RequestBody RemoveLinkRequest request) {
        LinkResponse response = linksService.removeLink(tgChatId, request);
        return ResponseEntity.ok(response);
    }
}
