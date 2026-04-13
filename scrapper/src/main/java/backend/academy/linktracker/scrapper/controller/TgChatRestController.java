package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.service.crud.ChatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tg-chat")
@RequiredArgsConstructor
public class TgChatRestController {
    private final ChatsService chatsService;

    @PostMapping("/{id}")
    public ResponseEntity<Void> registerChat(@PathVariable Long id) {
        chatsService.registerChat(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long id) {
        chatsService.deleteChat(id);
        return ResponseEntity.ok().build();
    }
}
