package backend.academy.linktracker.bot.mapper;

import backend.academy.linktracker.bot.dto.LinkUpdateMessage;
import backend.academy.linktracker.dto.kafka.LinkUpdateAvroMessage;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AvroMapper {
    LinkUpdateMessage fromAvro(LinkUpdateAvroMessage avro);

    default OffsetDateTime map(Instant value) {
        return OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
    }
}
