package backend.academy.linktracker.scrapper.model;

import backend.academy.linktracker.scrapper.model.value.TagId;
import lombok.Builder;
import lombok.Getter;

@Builder
public class Tag {
    @Getter
    private TagId tagId;

    @Getter
    private String name;
}
