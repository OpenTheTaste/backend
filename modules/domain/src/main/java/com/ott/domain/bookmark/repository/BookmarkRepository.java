package com.ott.domain.bookmark.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ott.domain.bookmark.domain.Bookmark;
import com.ott.domain.common.Status;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByMemberIdAndMediaIdAndStatus(Long memberId, Long mediaId, Status status);

    Optional<Bookmark> findByMemberIdAndMediaId(Long memberId, Long mediaId);

    @EntityGraph(attributePaths = {"media"})
    Page<Bookmark> findByMemberIdAndStatus(Long memberId, Status status, Pageable pageable);

}
