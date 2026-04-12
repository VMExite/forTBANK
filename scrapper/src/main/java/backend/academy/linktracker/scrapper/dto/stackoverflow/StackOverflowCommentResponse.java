package backend.academy.linktracker.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowCommentResponse(
        @JsonProperty("comment_id") Long commentId,
        @JsonProperty("post_id") Long postId,
        @JsonProperty("postType") String postType,
        @JsonProperty("body") String body,
        @JsonProperty("link") String link,
        @JsonProperty("creation_date") Long creationDate,
        @JsonProperty("owner") Owner owner) {
    public record Owner(@JsonProperty("display_name") String displayName) {}
}
