package backend.academy.linktracker.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StackExchangeResponse<T>(
        List<T> items,
        @JsonProperty("has_more") boolean hasMore,
        @JsonProperty("quota_remaining") int quotaRemaining) {}
