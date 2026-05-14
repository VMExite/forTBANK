package backend.academy.linktracker.scrapper.cache;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.mapper.LinkMapper;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.service.crud.LinksService;
import backend.academy.linktracker.scrapper.service.crud.impl.LinksServiceImpl;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 3)
@Fork(value = 1)
@State(Scope.Benchmark)
public class CacheBenchmark {

    private ConfigurableApplicationContext context;

    private LinksService linksService;
    private ChatRepository chatRepository;
    private CacheManager cacheManager;

    private static final long BASE_CHAT_ID = 42L;

    private final AtomicInteger counter = new AtomicInteger();

    @Setup(Level.Trial)
    public void setup() {

        context = new SpringApplicationBuilder(TestConfig.class)
                .web(WebApplicationType.NONE)
                .run();

        linksService = context.getBean(LinksService.class);
        chatRepository = context.getBean(ChatRepository.class);
        cacheManager = context.getBean(CacheManager.class);

        Chat chat = buildChat(BASE_CHAT_ID, 20);

        when(chatRepository.findById(new ChatId(BASE_CHAT_ID))).thenReturn(Optional.of(chat));

        linksService.getLinks(BASE_CHAT_ID);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        cacheManager.getCache("links").clear();
        context.close();
    }

    @Benchmark
    @Threads(8)
    public void cacheHit_throughput(Blackhole bh) {
        bh.consume(linksService.getLinks(BASE_CHAT_ID));
    }

    @Benchmark
    @Threads(4)
    public void cacheMiss_uniqueKeys(Blackhole bh) {

        long id = 10000L + counter.incrementAndGet();

        Chat chat = buildChat(id, 20);

        when(chatRepository.findById(new ChatId(id))).thenReturn(Optional.of(chat));

        bh.consume(linksService.getLinks(id));

        cacheManager.getCache("links").evict(id);
    }

    private Chat buildChat(long chatId, int linksCount) {

        Chat chat = Chat.builder().chatId(new ChatId(chatId)).build();

        for (int i = 0; i < linksCount; i++) {
            chat.addLink(Link.builder().url("https://example.com/" + i).build());
        }

        return chat;
    }

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("links");
        }

        @Bean
        public ChatRepository chatRepository() {
            return mock(ChatRepository.class);
        }

        @Bean
        public LinksService linksService(
                ChatRepository chatRepository, LinkMapper linkMapper, CacheManager cacheManager) {
            return new LinksServiceImpl(chatRepository, linkMapper);
        }
    }
}
