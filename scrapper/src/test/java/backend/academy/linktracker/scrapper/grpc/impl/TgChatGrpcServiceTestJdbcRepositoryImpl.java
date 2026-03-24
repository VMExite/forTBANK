package backend.academy.linktracker.scrapper.grpc.impl;

import backend.academy.linktracker.scrapper.grpc.AbstractTgChatGrpcServiceTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class TgChatGrpcServiceTestJdbcRepositoryImpl extends AbstractTgChatGrpcServiceTest {
    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("app.access-type", () -> "SQL");
    }
}
