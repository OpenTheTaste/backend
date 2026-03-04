package com.ott.domain.watch_history.repository;

import com.ott.domain.watch_history.domain.WatchHistory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long>, WatchHistoryRepositoryCustom {
    
    Optional<WatchHistory> findByMemberIdAndContentsId(Long memberId, Long contentsId);

}
