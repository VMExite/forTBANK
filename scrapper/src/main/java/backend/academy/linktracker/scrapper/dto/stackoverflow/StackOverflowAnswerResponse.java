package backend.academy.linktracker.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowAnswerResponse(
        @JsonProperty("answer_id") Long answerId,
        @JsonProperty("question_id") Long questionId,
        @JsonProperty("title") String title,
        @JsonProperty("body") String body,
        @JsonProperty("link") String link,
        @JsonProperty("creation_date") Long creationDate,
        @JsonProperty("owner") Owner owner) {
    public record Owner(@JsonProperty("display_name") String displayName) {}
}
