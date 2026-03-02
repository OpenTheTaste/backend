package com.ott.domain.watch_history.repository;

import lombok.Getter;

@Getter
public class RecentWatchProjection {

    private final Long contentsId;
    private final String posterUrl;
    private final Integer positionSec;
    private final Integer duration;

    public RecentWatchProjection(Long contentsId, String posterUrl, Integer positionSec, Integer duration) {
        this.contentsId = contentsId;
        this.posterUrl = posterUrl;
        this.positionSec = positionSec;
        this.duration = duration;
    }
}