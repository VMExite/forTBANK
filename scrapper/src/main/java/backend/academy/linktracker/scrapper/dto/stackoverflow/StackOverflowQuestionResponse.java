package backend.academy.linktracker.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowQuestionResponse(
        @JsonProperty("question_id") Long questionId,
        @JsonProperty("title") String title,
        @JsonProperty("link") String link) {}
