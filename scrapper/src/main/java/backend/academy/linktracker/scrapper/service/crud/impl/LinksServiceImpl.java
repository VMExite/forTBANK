package backend.academy.linktracker.scrapper.service.crud.impl;

import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinkResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.exception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyTracked;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.mapper.LinkMapper;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.service.crud.LinksService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LinksServiceImpl implements LinksService {

    private final ChatRepository chatRepository;
    private final LinkMapper linkMapper;

    @Override
    @Cacheable(value = "links", key = "#chatId")
    public ListLinkResponse getLinks(Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentException();
        }
        Chat chat = chatRepository.findById(new ChatId(chatId)).orElseThrow(ChatNotExistsException::new);
        List<LinkResponse> responses =
                chat.getLinks().stream().map(linkMapper::toResponse).toList();

        log.info("Links get from chat {}: size={}", chatId, responses.size());
        return new ListLinkResponse(responses, responses.size());
    }

    @Override
    @Transactional
    @CacheEvict(value = "links", key = "#chatId")
    public LinkResponse createLink(Long chatId, AddLinkRequest request) {
        if (chatId == null || request == null) {
            throw new IllegalArgumentException();
        }
        Chat chat = chatRepository.findById(new ChatId(chatId)).orElseThrow(ChatNotExistsException::new);
        if (chat.containsLink(request.link())) {
            throw new LinkAlreadyTracked();
        }

        Link link = linkMapper.fromAddRequest(request);
        chat.addLink(link);
        chatRepository.save(chat);
        log.info("Link {} created", link.getLinkId());
        return linkMapper.toResponse(link);
    }

    @Override
    @Transactional
    @CacheEvict(value = "links", key = "#chatId")
    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request) {

        if (chatId == null || request == null) {
            throw new IllegalArgumentException();
        }

        Chat chat = chatRepository.findById(new ChatId(chatId)).orElseThrow(ChatNotExistsException::new);
        Link link = chat.findLinkByUrl(request.link()).orElseThrow(LinkNotFoundException::new);

        chat.removeLink(link);
        chatRepository.save(chat);

        log.info("Link {} removed", link.getLinkId());

        return linkMapper.toResponse(link);
    }
}
