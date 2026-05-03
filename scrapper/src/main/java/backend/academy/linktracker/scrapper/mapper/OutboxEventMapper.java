package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.exception.OutboxMappingException;
import backend.academy.linktracker.scrapper.model.OutboxEvent;
import backend.academy.linktracker.scrapper.model.entity.OutboxEventEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.OffsetDateTime;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = IdsMapper.class)
public interface OutboxEventMapper {
    OutboxEvent fromEntity(OutboxEventEntity entity);

    OutboxEventEntity toEntity(OutboxEvent entity);

    @Mapping(target = "chatId", source = "payload", qualifiedByName = "extractChatId")
    @Mapping(target = "linkId", source = "payload", qualifiedByName = "extractLinkId")
    @Mapping(target = "title", source = "payload", qualifiedByName = "extractTitle")
    @Mapping(target = "username", source = "payload", qualifiedByName = "extractUsername")
    @Mapping(target = "preview", source = "payload", qualifiedByName = "extractPreview")
    @Mapping(target = "url", source = "payload", qualifiedByName = "extractUrl")
    @Mapping(target = "createdAt", source = "createdAt")
    LinkUpdateMessage toMessage(OutboxEvent event);

    @Mapping(target = "createdAt", expression = "java(batchTime)")
    @Mapping(target = "retryTime", expression = "java(batchTime)")
    @Mapping(target = "retryCount", constant = "0")
    @Mapping(target = "status", constant = "NEW")
    @Mapping(target = "type", constant = "LINK_UPDATE_EVENT_TYPE")
    @Mapping(target = "payload", expression = "java(serialize(message))")
    OutboxEvent toOutboxEvent(LinkUpdateMessage message, @Context OffsetDateTime batchTime);

    @Named("extractChatId")
    default Long extractChatId(String payload) {
        return parsePayload(payload).chatId();
    }

    @Named("extractLinkId")
    default Long extractLinkId(String payload) {
        return parsePayload(payload).linkId();
    }

    @Named("extractTitle")
    default String extractTitle(String payload) {
        return parsePayload(payload).title();
    }

    @Named("extractUsername")
    default String extractUsername(String payload) {
        return parsePayload(payload).username();
    }

    @Named("extractPreview")
    default String extractPreview(String payload) {
        return parsePayload(payload).preview();
    }

    @Named("extractUrl")
    default String extractUrl(String payload) {
        return parsePayload(payload).url();
    }

    default LinkUpdateMessage parsePayload(String payload) {
        try {
            return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(payload, LinkUpdateMessage.class);
        } catch (JsonProcessingException e) {
            throw new OutboxMappingException("Failed to deserialize payload: " + payload, e);
        }
    }

    default String serialize(LinkUpdateMessage message) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            return mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new OutboxMappingException("Failed to deserialize message: " + message, e);
        }
    }
}
