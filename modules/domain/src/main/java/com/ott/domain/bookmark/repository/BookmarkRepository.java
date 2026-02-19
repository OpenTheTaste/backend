package com.ott.domain.bookmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ott.domain.bookmark.domain.Bookmark;
import com.ott.domain.common.Status;
import com.ott.domain.common.TargetType;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByMemberIdAndTargetIdAndTargetTypeAndStatus(
            Long memberId,
            Long targetId,
            TargetType targetType,
            Status status);
}
