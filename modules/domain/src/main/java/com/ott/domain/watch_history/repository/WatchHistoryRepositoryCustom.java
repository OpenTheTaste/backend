package com.ott.domain.watch_history.repository;

import java.time.LocalDateTime;
import java.util.List;

public interface WatchHistoryRepositoryCustom {

    List<TagViewCountProjection> countByTagAndCategoryIdAndWatchedBetween(Long categoryId, LocalDateTime startDate, LocalDateTime endDate);

    //특정 회원의 최근 1달 시청이력 기반 태그 집계 (count 내림차순)
    List<TagRankingProjection> findTopTagsByMemberIdAndWatchedBetween(Long memberId, LocalDateTime startDate, LocalDateTime endDate);
}
