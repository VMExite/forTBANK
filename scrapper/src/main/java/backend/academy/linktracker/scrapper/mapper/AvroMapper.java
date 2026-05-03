package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.dto.kafka.LinkUpdateAvroMessage;
import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AvroMapper {
    LinkUpdateAvroMessage toAvro(LinkUpdateMessage message);

    default Instant map(OffsetDateTime value) {
        return value.toInstant();
    }

    default OffsetDateTime map(Instant value) {
        return OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
    }
}
