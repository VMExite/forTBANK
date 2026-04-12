package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SchedulerConfig {
    private final SchedulerProperties schedulerProperties;

    @Bean
    public ExecutorService schedulerExecutor() {
        return Executors.newFixedThreadPool(schedulerProperties.getThreads());
    }
}
