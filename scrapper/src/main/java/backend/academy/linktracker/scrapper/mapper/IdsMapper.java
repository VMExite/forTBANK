package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.scrapper.model.value.ChatId;
import backend.academy.linktracker.scrapper.model.value.EventId;
import backend.academy.linktracker.scrapper.model.value.LinkId;
import backend.academy.linktracker.scrapper.model.value.TagId;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IdsMapper {
    default Long map(ChatId id) {
        if (id == null) {
            return null;
        }
        return id.value();
    }

    default ChatId mapToChatId(Long in) {
        if (in == null) {
            return null;
        }
        return new ChatId(in);
    }

    default Long map(LinkId id) {
        if (id == null) {
            return null;
        }
        return id.value();
    }

    default LinkId mapToLinkId(Long in) {
        if (in == null) {
            return null;
        }
        return new LinkId(in);
    }

    default Long map(TagId id) {
        if (id == null) {
            return null;
        }
        return id.value();
    }

    default TagId mapToTagId(Long in) {
        if (in == null) {
            return null;
        }
        return new TagId(in);
    }

    default EventId mapToEventId(Long in) {
        if (in == null) {
            return null;
        }
        return new EventId(in);
    }

    default Long map(EventId id) {
        if (id == null) {
            return null;
        }
        return id.id();
    }
}
