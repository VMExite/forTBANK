package backend.academy.linktracker.scrapper.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Link {
    @EqualsAndHashCode.Include
    private Long id;
    private String url;
    private List<String> tags;
    private List<String> filters;

    private final Set<Chat> chats = new HashSet<>();
}
