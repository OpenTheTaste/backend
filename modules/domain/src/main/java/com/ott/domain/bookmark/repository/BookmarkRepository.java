package com.ott.domain.bookmark.repository;

import com.ott.domain.common.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ott.domain.bookmark.domain.Bookmark;
import com.ott.domain.common.Status;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByMemberIdAndMediaIdAndStatus(Long memberId, Long mediaId, Status status);

    Optional<Bookmark> findByMemberIdAndMediaId(Long memberId, Long mediaId);

    // 콘텐츠 북마크 목록 조회 (CONTENTS, SERIES)
    @EntityGraph(attributePaths = {"media"})
    Page<Bookmark> findByMemberIdAndStatusAndMedia_MediaTypeInOrderByCreatedDateDesc(
            Long memberId, Status status, List<MediaType> mediaTypes, Pageable pageable);

    // 숏폼 북마크 목록 (SHORT_FORM)
    @EntityGraph(attributePaths = {"media"})
    Page<Bookmark> findByMemberIdAndStatusAndMedia_MediaTypeOrderByCreatedDateDesc(
            Long memberId, Status status, MediaType mediaType, Pageable pageable);

    // 회원탈퇴 soft delete
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Bookmark b SET b.status = 'DELETE' WHERE b.member.id = :memberId")
    void softDeleteAllByMemberId(@Param("memberId") Long memberId);


    // 회원 탈퇴 전 ACTIVE인 유저가 북마크 row 상태 변경
    @Modifying(clearAutomatically = true)
    @Query(value = """
    UPDATE media m
    JOIN (
        SELECT b.media_id
        FROM bookmark b
        WHERE b.member_id = :memberId
          AND b.status = 'ACTIVE'
    ) t ON t.media_id = m.id
    SET m.bookmark_count = GREATEST(0, m.bookmark_count - 1)
    """, nativeQuery = true)
    void decreaseBookmarkCountByMemberId(@Param("memberId") Long memberId);

}
