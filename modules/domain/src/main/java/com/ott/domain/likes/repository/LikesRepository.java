package com.ott.domain.likes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ott.domain.common.Status;
import com.ott.domain.likes.domain.Likes;

import java.util.Optional;

import org.springframework.data.domain.Pageable;

public interface LikesRepository extends JpaRepository<Likes, Long> {
        boolean existsByMemberIdAndMediaIdAndStatus(Long memberId, Long mediaId, Status status);

        Optional<Likes> findByMemberIdAndMediaIdAndStatus(Long memberId, Long mediaId, Status status);

        Optional<Likes> findByMemberIdAndMediaId(Long memberId, Long mediaId);

        // 최근 좋아요한 미디어의 태그 ID 조회
        @Query("""
                        SELECT mt.tag.id FROM Likes l
                        JOIN MediaTag mt ON l.media.id = mt.media.id
                        WHERE l.member.id = :memberId AND l.status = :status
                        ORDER BY l.createdDate DESC
                        """)
        List<Long> findRecentTagIdsByMemberId(@Param("memberId") Long memberId, @Param("status") Status status,
                        Pageable pageable);
}
