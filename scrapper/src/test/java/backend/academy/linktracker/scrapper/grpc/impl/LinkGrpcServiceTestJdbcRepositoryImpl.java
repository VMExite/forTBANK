package backend.academy.linktracker.scrapper.grpc.impl;

import backend.academy.linktracker.scrapper.grpc.AbstractLinkGrpcServiceTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class LinkGrpcServiceTestJdbcRepositoryImpl extends AbstractLinkGrpcServiceTest {
    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("app.access-type", () -> "SQL");
    }
}
