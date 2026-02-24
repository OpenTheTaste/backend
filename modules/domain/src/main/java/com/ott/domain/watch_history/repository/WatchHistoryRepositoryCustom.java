package com.ott.domain.watch_history.repository;

import java.time.LocalDateTime;
import java.util.List;

public interface WatchHistoryRepositoryCustom {

    List<TagViewCountProjection> countByTagAndCategoryIdAndWatchedBetween(Long categoryId, LocalDateTime startDate, LocalDateTime endDate);
}
