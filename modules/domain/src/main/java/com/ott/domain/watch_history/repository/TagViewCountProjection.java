package com.ott.domain.watch_history.repository;

public record TagViewCountProjection(
        String tagName,
        Long viewCount
) {
}
