package backend.academy.linktracker.scrapper.mapper;


import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.entity.ChatEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = IdsMapper.class)
public interface ChatMapper {
    Chat fromEntity(ChatEntity chatEntity);
    ChatEntity toEntity(Chat chat);

    Chat fromId(Long id);
}
