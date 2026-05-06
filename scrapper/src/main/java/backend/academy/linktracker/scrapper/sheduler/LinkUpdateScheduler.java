package backend.academy.linktracker.scrapper.sheduler;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.mapper.OutboxEventMapper;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.OutboxEvent;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.service.crud.ChatsService;
import backend.academy.linktracker.scrapper.service.crud.LinkUpdateService;
import backend.academy.linktracker.scrapper.service.crud.OutboxEventService;
import backend.academy.linktracker.scrapper.service.executor.LinkExecutorHandler;
import com.google.common.collect.Lists;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LinkUpdateScheduler {
    private final SchedulerProperties schedulerProperties;
    private final LinkUpdateService linkUpdateService;
    private final ChatsService chatsService;
    private final LinkExecutorHandler executorHandler;
    private final OutboxEventService outboxEventService;
    private final ExecutorService executorService;
    private final OutboxEventMapper outboxEventMapper;

    public LinkUpdateScheduler(
            SchedulerProperties schedulerProperties,
            LinkUpdateService linkUpdateService,
            ChatsService chatsService,
            LinkExecutorHandler executorHandler,
            OutboxEventService outboxEventService,
            @Qualifier("schedulerExecutor") ExecutorService executorService,
            OutboxEventMapper outboxEventMapper) {
        this.schedulerProperties = schedulerProperties;
        this.linkUpdateService = linkUpdateService;
        this.chatsService = chatsService;
        this.executorHandler = executorHandler;
        this.outboxEventService = outboxEventService;
        this.executorService = executorService;
        this.outboxEventMapper = outboxEventMapper;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.delay}")
    public void update() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Link> batch = linkUpdateService.getBatch(schedulerProperties.getBatchSize(), now);
        if (batch.isEmpty()) {
            return;
        }
        processBatch(batch, now);
    }

    private void processBatch(List<Link> batch, OffsetDateTime batchTime) {
        int threads = schedulerProperties.getThreads();
        List<List<Link>> partitions = Lists.partition(batch, (int) Math.ceil((double) batch.size() / threads));

        Map<Link, List<ChatId>> failedLinks = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> futures = partitions.stream()
                .map(part ->
                        CompletableFuture.runAsync(() -> processChunk(part, failedLinks, batchTime), executorService))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> {
                    log.error("Batch processing failed", ex);
                    return null;
                })
                .join();

        if (!failedLinks.isEmpty()) {
            processFailedLinks(failedLinks, batchTime);
        }
    }

    private void processChunk(List<Link> links, Map<Link, List<ChatId>> failedLinks, OffsetDateTime batchTime) {

        for (Link link : links) {
            List<ChatId> chatIds = chatsService.getChatIdsByLink(link);
            try {
                List<LinkUpdateMessage> messages = executorHandler.execute(link, chatIds);
                List<OutboxEvent> events = messages.stream()
                        .map(msg -> outboxEventMapper.toOutboxEvent(msg, batchTime))
                        .toList();

                outboxEventService.save(events);
                linkUpdateService.saveLastUpdates(link, messages);
            } catch (Exception e) {
                log.warn("Failed to process link={}", link.getUrl(), e);
                failedLinks.put(link, chatIds);
            }
        }
    }

    private void processFailedLinks(Map<Link, List<ChatId>> failedLinks, OffsetDateTime batchTime) {

        List<OutboxEvent> events = failedLinks.entrySet().stream()
                .flatMap(entry -> {
                    Link link = entry.getKey();
                    return entry.getValue().stream().map(chatId -> {
                        LinkUpdateMessage msg = new LinkUpdateMessage(
                                null,
                                chatId.value(),
                                link.getLinkId().value(),
                                "Failed process link",
                                null,
                                null,
                                link.getUrl(),
                                batchTime);
                        return outboxEventMapper.toOutboxEvent(msg, batchTime);
                    });
                })
                .toList();

        outboxEventService.save(events);
    }
}
