package backend.academy.linktracker.scrapper.dto;

import java.util.List;

public record StackOverflowResponse(List<StackOverflowQuestionItem> items) {}
