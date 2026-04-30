package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.properties.OutboxProperties;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SchedulerConfig {
    private final SchedulerProperties schedulerProperties;
    private final OutboxProperties outboxProperties;

    @Bean(value = "schedulerExecutor")
    public ExecutorService schedulerExecutor() {
        return new ThreadPoolExecutor(
            schedulerProperties.getThreads(),
            schedulerProperties.getThreads(),
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder()
                .setNameFormat("link-scheduler-%d")
                .build()
        );
    }

    @Bean("outboxExecutor")
    public ExecutorService outboxExecutor() {
        return new ThreadPoolExecutor(
            outboxProperties.getThreads(),
            outboxProperties.getThreads(),
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder()
                .setNameFormat("outbox-worker-%d")
                .build()
        );
    }

    public void gracefulShutdown() {
        shutdownExecutor(schedulerExecutor(), "schedulerExecutor",
            schedulerProperties.getTerminationAwaitMillis());
        shutdownExecutor(outboxExecutor(), "outboxExecutor",
            outboxProperties.getTerminationAwaitMillis());
    }

    private void shutdownExecutor(ExecutorService executor, String name, long awaitMillis) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(awaitMillis, TimeUnit.MILLISECONDS)) {
                log.warn("Shutdown of {} timed out", name);
                List<Runnable> tasks = executor.shutdownNow();
                log.warn("{} abruptly shut down, {} tasks dropped", name, tasks.size());
            } else {
                log.info("{} shut down gracefully", name);
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            log.warn("Interrupted while shutting down {}", name, e);
        }
    }
}
