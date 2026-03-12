package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.properties.ScrapperGrpcProperties;
import backend.academy.linktracker.scrapper.LinkServiceGrpc;
import backend.academy.linktracker.scrapper.TgChatServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GrpcClientsConfig {
    private final ScrapperGrpcProperties scrapperGrpcProperties;

    @Bean
    ManagedChannel scrapperManagedChannel() {
        return ManagedChannelBuilder
            .forAddress(scrapperGrpcProperties.getHost(), scrapperGrpcProperties.getPort())
            .usePlaintext()
            .build();
    }

    @Bean
    LinkServiceGrpc.LinkServiceBlockingStub linkServiceBlockingStub(ManagedChannel scrapperManagedChannel) {
        return LinkServiceGrpc.newBlockingStub(scrapperManagedChannel);
    }

    @Bean
    TgChatServiceGrpc.TgChatServiceBlockingStub tgChatServiceBlockingStub(ManagedChannel scrapperManagedChannel) {
        return TgChatServiceGrpc.newBlockingStub(scrapperManagedChannel);
    }
}
