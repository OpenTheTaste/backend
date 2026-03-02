package com.ott.domain.playback.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ott.domain.common.Status;
import com.ott.domain.playback.domain.Playback;

public interface PlaybackRepository extends JpaRepository<Playback, Long> {
        // 가장 최신으로 하나만 가져오기
        // Optional<Playback> findFirstByMemberIdAndContentsIdAndStatus(Long memberId,
        // Long contentsId, Status status);

        // 최신 시청 이력 100개 가져오기 - 선호 태그 조사용
        @Query("""
                        SELECT mt.tag.id FROM Playback p
                        JOIN MediaTag mt ON p.contents.media.id  = mt.media.id
                        WHERE p.member.id = :memberId AND p.status = :status
                        ORDER BY p.modifiedDate DESC
                        """)
        List<Long> findRecentTagIdsByMemberId(
                        @Param("memberId") Long memberId,
                        @Param("status") Status status,
                        Pageable pageable); // pageable로 100개로 리미트 제한
}
