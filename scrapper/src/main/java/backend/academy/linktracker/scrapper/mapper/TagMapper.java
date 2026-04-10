package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.model.entity.TagEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = IdsMapper.class)
public interface TagMapper {
    Tag fromEntity(TagEntity tag);
    TagEntity toEntity(Tag tag);
}
