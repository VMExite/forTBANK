package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowQuestionItem(
    @JsonProperty("last_activity_date")
    Long lastActivityDate
) {
}
