package com.ott.domain.media.repository;

import lombok.Getter;

@Getter
public class TagContentProjection {

    private final Long mediaId;
    private final String posterUrl;

    public TagContentProjection(Long mediaId, String posterUrl) {
        this.mediaId = mediaId;
        this.posterUrl = posterUrl;
    }
}