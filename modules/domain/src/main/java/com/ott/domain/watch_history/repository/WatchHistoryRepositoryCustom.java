package com.ott.domain.watch_history.repository;

import java.time.LocalDateTime;
import java.util.List;

public interface WatchHistoryRepositoryCustom {

    List<TagViewCountProjection> countByTagAndCategoryIdAndWatchedBetween(Long categoryId, LocalDateTime startDate, LocalDateTime endDate);

    // 가장 최근에 시청한 에피소드의 media_id 반환
    Optional<Long> findLatestContentMediaIdByMemberIdAndSeriesId(Long memberId, Long seriesId);

}
