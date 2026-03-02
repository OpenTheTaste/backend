package com.ott.domain.watch_history.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface WatchHistoryRepositoryCustom {

    List<TagViewCountProjection> countByTagAndCategoryIdAndWatchedBetween(Long categoryId, LocalDateTime startDate, LocalDateTime endDate);

    //특정 회원의 최근 1달 시청이력 기반 태그 집계 (count 내림차순)
    List<TagRankingProjection> findTopTagsByMemberIdAndWatchedBetween(Long memberId, LocalDateTime startDate, LocalDateTime endDate);

    // 특정 태그의 2달 시청이력 기반 count 집계
    Long countByMemberIdAndTagIdAndWatchedBetween(Long memberId, Long tagId, LocalDateTime startDate, LocalDateTime endDate);

    // 특정 회원의 전체 시청이력 페이징 조회 (최신순)
    Page<RecentWatchProjection> findWatchHistoryByMemberId(Long memberId, Pageable pageable);
}
