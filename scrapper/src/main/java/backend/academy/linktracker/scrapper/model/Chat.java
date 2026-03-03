package backend.academy.linktracker.scrapper.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Chat {
    @EqualsAndHashCode.Include
    private Long id;

    private final Set<Link> links = new HashSet<>();

    public void addLink(Link link) {
        links.add(link);
        link.getChats().add(this);
    }

    public void removeLink(Link link) {
        links.remove(link);
        link.getChats().remove(this);
    }

}
