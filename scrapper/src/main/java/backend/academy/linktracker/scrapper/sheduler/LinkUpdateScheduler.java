package backend.academy.linktracker.scrapper.sheduler;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.service.crud.LinkUpdateService;
import backend.academy.linktracker.scrapper.service.executor.LinkExecutorHandler;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdateScheduler {
    private final SchedulerProperties schedulerProperties;
    private final LinkUpdateService linkUpdateService;
    private final LinkExecutorHandler executorHandler;
    private final MessageSender sender;
    private final ExecutorService executorService;

    @Scheduled(fixedDelayString = "${app.scheduler.delay}")
    public void update() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Link> batch = linkUpdateService.getBatch(schedulerProperties.getBatchSize(), now);
        if (batch.isEmpty()) {
            return;
        }
        processBatch(batch);
    }

    private void processBatch(List<Link> batch) {
        long threads = schedulerProperties.getThreads();
        List<List<Link>> partitions = partition(batch, threads);
        List<Future<?>> futures = new ArrayList<>();
        for (List<Link> part : partitions) {
            futures.add(executorService.submit(() -> processChunk(part)));
        }
        waitAll(futures);
    }

    private void processChunk(List<Link> links) {
        for (Link link : links) {
            try {
                List<LinkUpdateMessage> messages = executorHandler.execute(link);
                for (LinkUpdateMessage msg : messages) {
                    sender.sendMessage(msg);
                }

                linkUpdateService.saveLastUpdates(link, messages);
            } catch (Exception e) {
                log.error("Failed to process link={}", link.getUrl(), e);
            }
        }
    }

    private void waitAll(List<Future<?>> futures) {
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Thread execution failed", e);
            }
        }
    }

    private List<List<Link>> partition(List<Link> list, long parts) {
        int size = list.size();
        int chunkSize = (int) Math.ceil((double) size / parts);
        List<List<Link>> result = new ArrayList<>();
        for (int i = 0; i < size; i += chunkSize) {
            result.add(list.subList(i, Math.min(i + chunkSize, size)));
        }

        return result;
    }
}
