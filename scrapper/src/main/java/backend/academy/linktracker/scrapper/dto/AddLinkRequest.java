package backend.academy.linktracker.scrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddLinkRequest {
    private String link;
    private List<String> tags;
    private List<String> filters;
}
