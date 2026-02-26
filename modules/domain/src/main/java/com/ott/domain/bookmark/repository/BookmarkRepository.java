package com.ott.domain.bookmark.repository;

import com.ott.domain.common.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ott.domain.bookmark.domain.Bookmark;
import com.ott.domain.common.Status;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByMemberIdAndMediaIdAndStatus(Long memberId, Long mediaId, Status status);

    Optional<Bookmark> findByMemberIdAndMediaId(Long memberId, Long mediaId);

    // 콘텐츠 목록 조회 (CONTENTS, SERIES)
    Page<Bookmark> findByMemberIdAndStatusAndMedia_MediaTypeIn(
            Long memberId, Status status, List<MediaType> mediaTypes, Pageable pageable);

}
