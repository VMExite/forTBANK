package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.UpdateNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updates")
@RequiredArgsConstructor
public class UpdatesRestController {
    private final UpdateNotificationService updateNotificationService;

    @PostMapping
    public ResponseEntity<Void> handleUpdate(@Valid @RequestBody LinkUpdate update) {
        updateNotificationService.notifyUsers(update);
        return ResponseEntity.ok().build();
    }
}
