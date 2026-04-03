package com.ott.api_admin.tagging.event;

public record AiTaggingRequestedEvent(Long mediaId, String description) {
}
