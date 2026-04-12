package backend.academy.linktracker.scrapper.model;

import backend.academy.linktracker.scrapper.model.value.LinkId;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public class Link {
    @Getter
    private LinkId linkId;

    @Getter
    private String url;

    @Getter
    @Setter
    @Builder.Default
    private OffsetDateTime lastUpdate = OffsetDateTime.now();

    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    public boolean containsTag(String tag) {
        return tags.stream().map(Tag::getName).anyMatch(t -> t.equalsIgnoreCase(tag));
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }
}
