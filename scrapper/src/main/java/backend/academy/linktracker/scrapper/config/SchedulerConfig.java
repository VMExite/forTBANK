package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SchedulerConfig {
    private final SchedulerProperties schedulerProperties;
    @Setter
    private ExecutorService executorService;

    @Bean
    public ExecutorService schedulerExecutor() {
        return Executors.newFixedThreadPool(schedulerProperties.getThreads());
    }

    @PreDestroy
    public void gracefulShutdown() {
        executorService.shutdown();
        try {
            if (!executorService
                .awaitTermination(
                    schedulerProperties.getTerminationAwaitMillis(),
                    TimeUnit.MILLISECONDS
                )) {

                log.warn("Shutdown of scheduler executor timed out");
                List<Runnable> tasks = executorService.shutdownNow();
                log.warn("Executor was abruptly shut down. {}", tasks.size());
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            log.warn("Interrupted while shutting down scheduler executor.", e);
        }
    }
}
