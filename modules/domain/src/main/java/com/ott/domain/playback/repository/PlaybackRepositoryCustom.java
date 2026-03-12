package com.ott.domain.playback.repository;

import com.ott.domain.playback.domain.Playback;

import java.util.List;

public interface PlaybackRepositoryCustom {

    List<Playback> findLatestByMemberAndSeriesMediaIds(Long memberId, List<Long> seriesMediaIdList);
}
