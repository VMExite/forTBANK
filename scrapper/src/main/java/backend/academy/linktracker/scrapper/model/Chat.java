package backend.academy.linktracker.scrapper.model;

import backend.academy.linktracker.scrapper.exception.LinkAlreadyTracked;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Builder
public class Chat {
    @Getter
    private ChatId chatId;

    @Builder.Default
    private Set<Link> links = new HashSet<>();

    public boolean containsLink(String url) {
        return links.stream().anyMatch(link -> link.getUrl().equals(url));
    }

    public Optional<Link> findLinkByUrl(String url) {
        return links.stream().filter(link -> link.getUrl().equals(url)).findFirst();
    }

    public void addLink(Link link) throws LinkAlreadyTracked {
        if (containsLink(link.getUrl())) {
            throw new LinkAlreadyTracked();
        }
        links.add(link);
    }

    public void removeLink(Link link) {
        links.remove(link);
    }

    public Set<Link> getLinks() {
        return Collections.unmodifiableSet(links);
    }
}
