package com.ott.domain.contents.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

// import java.util.List;
//
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
//
// import com.ott.domain.common.Status;
// import com.ott.domain.contents.domain.Contents;
//
// public interface ContentsRepository extends JpaRepository<Contents, Long> {
//
// // 제목에 검색어 포함, 상태 ACTIVE, 시리즈 없는 콘텐츠만 검색 (최신순 정렬)
// @Query("SELECT c FROM Contents c " +
// "WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
// "AND c.status = :status " +
// "AND c.series IS NULL " +
// "ORDER BY c.createdDate DESC")
// List<Contents> searchLatest(@Param("keyword") String searchWord,
// @Param("status") Status status,
// Pageable pageable);
//
// }

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ott.domain.common.PublicStatus;
import com.ott.domain.common.Status;
import com.ott.domain.contents.domain.Contents;

import java.util.Optional;

public interface ContentsRepository extends JpaRepository<Contents, Long>, ContentsRepositoryCustom {

        @EntityGraph(attributePaths = { "media" })
        Page<Contents> findBySeriesIdAndStatusAndMedia_PublicStatusOrderByIdAsc(Long seriesId, Status status,
                        PublicStatus publicStatus, Pageable pageable);

        // 좋아요 처리 시 series 소속 여부 확인용
        @EntityGraph(attributePaths = { "series", "series.media" })
        Optional<Contents> findByMediaId(Long mediaId);

        @Query("""
                SELECT c FROM Contents c
                JOIN FETCH c.media m
                WHERE c.id = :contentsId
                AND c.status = :status
                AND m.publicStatus = :publicStatus
                """)
        Optional<Contents> findByIdAndStatusAndMedia_PublicStatus(
                        @Param("contentsId") Long contentsId,
                        @Param("status") Status status,
                        @Param("publicStatus") PublicStatus publicStatus);

        boolean existsByIdAndStatus(Long id, Status status);
}