package backend.academy.linktracker.scrapper.repository.jpa.impl;

import backend.academy.linktracker.scrapper.mapper.ChatMapper;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.entity.ChatEntity;
import backend.academy.linktracker.scrapper.model.entity.LinkEntity;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import backend.academy.linktracker.scrapper.model.value.LinkId;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.jpa.ChatJpaRepository;
import backend.academy.linktracker.scrapper.repository.jpa.LinkJpaRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public class ChatJpaAdapter implements ChatRepository {

    private final ChatJpaRepository chatJpaRepository;
    private final LinkJpaRepository linkJpaRepository;
    private final ChatMapper chatMapper;

    @Override
    @Transactional
    public Optional<Chat> findById(ChatId id) {
        return chatJpaRepository.findWithGraphByChatId(id.value()).map(chatMapper::fromEntity);
    }

    @Override
    public List<ChatId> findChatIdByLinkId(LinkId listId) {
        return chatJpaRepository.findChatIdsByLinkId(listId.value()).stream()
                .map(ChatId::new)
                .toList();
    }

    @Override
    @Transactional
    public Chat save(Chat chat) {

        ChatEntity entity = chatJpaRepository.findById(chat.getChatId().value()).orElseGet(() -> ChatEntity.builder()
                .chatId(chat.getChatId().value())
                .links(new HashSet<>())
                .build());
        Set<LinkEntity> newLinks = new HashSet<>();

        // Todo: need fix
        for (Link link : chat.getLinks()) {
            LinkEntity linkEntity = linkJpaRepository
                    .findByUrl(link.getUrl())
                    .orElseGet(() -> linkJpaRepository.save(LinkEntity.builder()
                            .url(link.getUrl())
                            .lastUpdate(link.getLastUpdate())
                            .tags(new HashSet<>())
                            .build()));

            newLinks.add(linkEntity);
        }

        entity.getLinks().clear();
        entity.getLinks().addAll(newLinks);

        return chatMapper.fromEntity(chatJpaRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteById(ChatId id) {
        chatJpaRepository.deleteById(id.value());
    }
}
