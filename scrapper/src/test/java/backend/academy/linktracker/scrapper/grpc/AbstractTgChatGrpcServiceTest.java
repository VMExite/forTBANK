package backend.academy.linktracker.scrapper.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.linktracker.scrapper.ScrapperProto;
import backend.academy.linktracker.scrapper.TgChatServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
public abstract class AbstractTgChatGrpcServiceTest {

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

    private ManagedChannel channel;
    private TgChatServiceGrpc.TgChatServiceBlockingStub stub;

    @BeforeEach
    void setUp() {
        channel = InProcessChannelBuilder.forName(IN_PROCESS_NAME)
                .directExecutor()
                .build();
        stub = TgChatServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.shutdownNow();
        }
    }

    @Test
    void registerThenDeleteChat() {
        long chatId = 101L;

        ScrapperProto.ChatResponse created = stub.registerChat(
                ScrapperProto.RegisterChatRequest.newBuilder().setId(chatId).build());
        assertThat(created.getMessage()).isEqualTo("чат добавлен");

        ScrapperProto.ChatResponse deleted = stub.deleteChat(
                ScrapperProto.DeleteChatRequest.newBuilder().setId(chatId).build());
        assertThat(deleted.getMessage()).isEqualTo("чат удален");
    }

    @Test
    void registerDuplicateChatReturnsAlreadyExists() {
        long chatId = 202L;

        stub.registerChat(
                ScrapperProto.RegisterChatRequest.newBuilder().setId(chatId).build());

        assertThatThrownBy(() -> stub.registerChat(ScrapperProto.RegisterChatRequest.newBuilder()
                        .setId(chatId)
                        .build()))
                .isInstanceOf(StatusRuntimeException.class)
                .extracting(ex -> ((StatusRuntimeException) ex).getStatus().getCode())
                .isEqualTo(Status.ALREADY_EXISTS.getCode());
    }

    @Test
    void deleteMissingChatReturnsNotFound() {
        assertThatThrownBy(() -> stub.deleteChat(
                        ScrapperProto.DeleteChatRequest.newBuilder().setId(999L).build()))
                .isInstanceOf(StatusRuntimeException.class)
                .extracting(ex -> ((StatusRuntimeException) ex).getStatus().getCode())
                .isEqualTo(Status.NOT_FOUND.getCode());
    }
}
