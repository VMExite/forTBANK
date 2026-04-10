package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.model.entity.LinkEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = IdsMapper.class)
public interface LinkMapper {
    Link fromEntity(LinkEntity linkEntity);
    LinkEntity toEntity(Link link);

    LinkResponse toResponse(Link link);
    @Mapping(source = "link", target = "url")
    Link fromAddRequest(AddLinkRequest addLinkRequest);

    default String mapToString(Tag tag) {
        return tag.getName();
    }

    default Tag fromStringToTag(String string) {
        return Tag.builder().name(string).build();
    }
}
