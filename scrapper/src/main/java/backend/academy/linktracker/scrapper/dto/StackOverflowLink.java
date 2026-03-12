package backend.academy.linktracker.scrapper.dto;

public record StackOverflowLink(Long questionId) implements ParsedLink {}
