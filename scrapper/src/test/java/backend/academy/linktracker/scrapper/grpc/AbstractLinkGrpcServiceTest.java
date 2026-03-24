package backend.academy.linktracker.scrapper.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.linktracker.scrapper.LinkServiceGrpc;
import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.service.RegistrationService;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
@Sql(scripts = "classpath:/sql/clear.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class AbstractLinkGrpcServiceTest {

    private static final String IN_PROCESS_NAME = "test-grpc-tg";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")
            .withUrlParam("ssl", "false");

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("grpc.server.in-process-name", () -> IN_PROCESS_NAME);
    }

    @Autowired
    private RegistrationService registrationService;

    private ManagedChannel channel;
    private LinkServiceGrpc.LinkServiceBlockingStub stub;

    @BeforeEach
    void setUp() {
        channel = InProcessChannelBuilder.forName(IN_PROCESS_NAME)
                .directExecutor()
                .build();
        stub = LinkServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.shutdownNow();
        }
    }

    @Test
    void createAndGetLinks() {
        registrationService.registerChat(1L);

        ScrapperProto.AddLinkRequest addRequest = ScrapperProto.AddLinkRequest.newBuilder()
                .setTgChatId(1L)
                .setLink("https://example.com")
                .addAllTags(List.of("java", "grpc"))
                .build();

        ScrapperProto.LinkResponse created = stub.createLink(addRequest);
        assertThat(created.getUrl()).isEqualTo("https://example.com");

        ScrapperProto.GetLinkRequest getRequest =
                ScrapperProto.GetLinkRequest.newBuilder().setTgChatId(1L).build();

        ScrapperProto.ListLinkResponse list = stub.getLinks(getRequest);
        assertThat(list.getLinksCount()).isEqualTo(1);
        assertThat(list.getLinks(0).getUrl()).isEqualTo("https://example.com");
        assertThat(list.getLinks(0).getTagsList()).containsExactlyInAnyOrder("java", "grpc");
    }

    @Test
    void createLinkForMissingChatReturnsNotFound() {
        ScrapperProto.AddLinkRequest addRequest = ScrapperProto.AddLinkRequest.newBuilder()
                .setTgChatId(999L)
                .setLink("https://example.com")
                .build();

        assertThatThrownBy(() -> stub.createLink(addRequest))
                .isInstanceOf(StatusRuntimeException.class)
                .extracting(ex -> ((StatusRuntimeException) ex).getStatus().getCode())
                .isEqualTo(Status.NOT_FOUND.getCode());
    }
}
