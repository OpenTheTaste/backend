package com.ott.domain.playback.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ott.domain.common.Status;
import com.ott.domain.playback.domain.Playback;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaybackRepository extends JpaRepository<Playback, Long> {
    // 가장 최신으로 하나만 가져오기
    // Optional<Playback> findFirstByMemberIdAndContentsIdAndStatus(Long memberId,
    // Long contentsId, Status status);

    // 회원 탈퇴
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Playback p SET p.status = 'DELETE' WHERE p.member.id = :memberId")
    void softDeleteAllByMemberId(@Param("memberId") Long memberId);
}
