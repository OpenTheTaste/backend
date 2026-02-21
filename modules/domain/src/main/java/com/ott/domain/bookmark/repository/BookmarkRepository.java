package com.ott.domain.bookmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ott.domain.bookmark.domain.Bookmark;
import com.ott.domain.common.Status;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByMemberIdAndMediaIdAndStatus(Long memberId, Long mediaId, Status status);
}
