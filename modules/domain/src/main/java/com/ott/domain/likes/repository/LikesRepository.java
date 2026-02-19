package com.ott.domain.likes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ott.domain.common.Status;
import com.ott.domain.common.TargetType;
import com.ott.domain.likes.domain.Likes;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    boolean existsByMemberIdAndTargetIdAndTargetTypeAndStatus(
            Long memberId,
            Long targetId,
            TargetType targetType,
            Status status);
}
