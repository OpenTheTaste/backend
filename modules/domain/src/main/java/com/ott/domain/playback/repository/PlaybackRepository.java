package com.ott.domain.playback.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ott.domain.common.Status;
import com.ott.domain.playback.domain.Playback;
import org.springframework.data.jpa.repository.Modifying;

public interface PlaybackRepository extends JpaRepository<Playback, Long> {

    // 회원 탈퇴
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Playback p SET p.status = 'DELETE' WHERE p.member.id = :memberId")
    void softDeleteAllByMemberId(@Param("memberId") Long memberId);


    // 최근 시청한 미디어의 태그 ID 조회
    // 1단계 - 최근 시청 이력 100개 가져오기 (JOIN 보다 LIMIT 먼저)
    @Query("""
            SELECT p.contents.media.id FROM Playback p
            WHERE p.member.id = :memberId AND p.status = :status
            ORDER BY p.modifiedDate DESC
            """)
        List<Long> findRecentPlayedMediaIds(
                @Param("memberId") Long memberId,
                @Param("status") Status status,
                Pageable pageable);
}
