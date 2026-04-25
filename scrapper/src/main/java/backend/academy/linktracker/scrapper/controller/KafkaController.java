package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.service.sender.impl.KafkaMessageSender;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/kafka")
@RequiredArgsConstructor
public class KafkaController {
    private final KafkaMessageSender kafkaProducer;
    @PostMapping
    public ResponseEntity<@NonNull LinkUpdateMessage> sendMessage(@RequestBody LinkUpdateMessage linkUpdateMessage) {
        log.info("Received request to send link update message: {}", linkUpdateMessage);
        kafkaProducer.sendMessage(linkUpdateMessage);
        return ResponseEntity.ok(linkUpdateMessage);
    }
}
