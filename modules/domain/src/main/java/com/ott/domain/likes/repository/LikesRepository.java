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
}
