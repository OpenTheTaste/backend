package com.ott.domain.likes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ott.domain.common.Status;
import com.ott.domain.likes.domain.Likes;

import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    boolean existsByMemberIdAndMediaIdAndStatus(Long memberId, Long mediaId, Status status);


   Optional<Likes> findByMemberIdAndMediaIdAndStatus(Long memberId, Long mediaId, Status status);
}
