package backend.academy.linktracker.scrapper.sheduler;

import backend.academy.linktracker.scrapper.service.LinkUpdateRestClientService;
import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.service.LinkUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LinkUpdateScheduler {
    private final LinkUpdateService service;
    private final LinkUpdateRestClientService controller;

    @Scheduled(fixedDelayString = "${spring.application.scheduler-interval}")
    public void update() {
        List<LinkUpdateRequest> updates = service.updateLinks();
        controller.updateLinks(updates);
    }
}
