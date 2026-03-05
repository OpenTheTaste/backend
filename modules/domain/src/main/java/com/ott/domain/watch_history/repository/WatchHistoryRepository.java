package com.ott.domain.watch_history.repository;

import com.ott.domain.watch_history.domain.WatchHistory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long>, WatchHistoryRepositoryCustom {

    // 회원 탈퇴
    @Modifying(clearAutomatically = true)
    @Query("UPDATE WatchHistory w SET w.status = 'DELETE' WHERE w.member.id = :memberId")
    void softDeleteAllByMemberId(@Param("memberId") Long memberId);
    
    Optional<WatchHistory> findByMemberIdAndContentsId(Long memberId, Long contentsId);

}
