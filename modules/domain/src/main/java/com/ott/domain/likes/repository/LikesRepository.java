package com.ott.domain.likes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ott.domain.common.Status;
import com.ott.domain.likes.domain.Likes;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    boolean existsByMemberIdAndMediaIdAndStatus(Long memberId, Long mediaId, Status status);


   Optional<Likes> findByMemberIdAndMediaIdAndStatus(Long memberId, Long mediaId, Status status);

   Optional<Likes> findByMemberIdAndMediaId(Long memberId, Long mediaId);

   // 회원 탈퇴 soft delete
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Likes l SET l.status = 'DELETE' WHERE l.member.id = :memberId")
    void softDeleteAllByMemberId(@Param("memberId") Long memberId);

    // 회원 탈퇴 시 해당 유저가 좋아요한 미디어에 대하여 -count
    @Modifying(clearAutomatically = true)
    @Query(value = """
    UPDATE media m
    JOIN (
        SELECT l.media_id, COUNT(*) AS cnt
        FROM likes l
        WHERE l.member_id = :memberId
          AND l.status = 'ACTIVE'
        GROUP BY l.media_id
    ) t ON t.media_id = m.id
    SET m.likes_count = GREATEST(0, m.likes_count - t.cnt)
    """, nativeQuery = true)
    void decreaseLikesCountByMemberId(@Param("memberId") Long memberId);
}
