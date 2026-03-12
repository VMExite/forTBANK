package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.BotServiceGrpc;
import backend.academy.linktracker.scrapper.properties.BotGrpcProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GrpcClientsConfig {
    private final BotGrpcProperties botGrpcProperties;

    @Bean
    ManagedChannel botManagedChannel() {
        return ManagedChannelBuilder
            .forAddress(botGrpcProperties.getHost(), botGrpcProperties.getPort())
            .usePlaintext()
            .build();
    }

    @Bean
    BotServiceGrpc.BotServiceBlockingStub botServiceBlockingStub(ManagedChannel botManagedChannel) {
        return BotServiceGrpc.newBlockingStub(botManagedChannel);
    }
}
