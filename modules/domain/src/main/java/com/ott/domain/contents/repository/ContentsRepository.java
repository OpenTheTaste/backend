package com.ott.domain.contents.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ott.domain.common.PublicStatus;
import com.ott.domain.common.Status;
import com.ott.domain.contents.domain.Contents;


public interface ContentsRepository extends JpaRepository<Contents, Long>, ContentsRepositoryCustom {

        // 상태 추가 중 기존 함수 이름을 유지하기 위해 쿼리로 변경했습니다.
        @EntityGraph(attributePaths = { "media" })
        @Query("""
                SELECT c FROM Contents c
                JOIN FETCH c.media m
                WHERE c.series.id = :seriesId
                AND c.status = :status
                AND m.publicStatus = :publicStatus
                AND m.mediaStatus = com.ott.domain.media.domain.MediaStatus.COMPLETED
                ORDER BY c.id ASC
                """)
        Page<Contents> findBySeriesIdAndStatusAndMedia_PublicStatusOrderByIdAsc(
                        @Param("seriesId") Long seriesId,
                        @Param("status") Status status,
                        @Param("publicStatus") PublicStatus publicStatus,
                        Pageable pageable);

        @EntityGraph(attributePaths = { "media" })
        @Query("""
                SELECT c FROM Contents c
                JOIN FETCH c.media m
                WHERE c.series.media.id = :seriesMediaId
                AND c.status = :status
                AND m.publicStatus = :publicStatus
                AND m.mediaStatus = com.ott.domain.media.domain.MediaStatus.COMPLETED
                ORDER BY c.id ASC
                """)
        Page<Contents> findBySeries_Media_IdAndStatusAndMedia_PublicStatusOrderByIdAsc(
                        @Param("seriesMediaId") Long seriesMediaId,
                        @Param("status") Status status,
                        @Param("publicStatus") PublicStatus publicStatus,
                        Pageable pageable);

        // 좋아요 처리 시 series 소속 여부 확인용
        @EntityGraph(attributePaths = {"series", "series.media"})
        Optional<Contents> findByMediaId(Long mediaId);

        // 댓글 작성 시 콘텐츠 조회
        @EntityGraph(attributePaths = {"media"})
        Optional<Contents> findByIdAndStatus(Long id, Status status);

        
        // 미디어 Id 로 해당 콘텐츠 조회 
        @Query("""
                SELECT c FROM Contents c
                JOIN FETCH c.media m
                WHERE m.id = :mediaId
                AND c.status = :status
                AND m.publicStatus = :publicStatus
                AND m.mediaStatus = com.ott.domain.media.domain.MediaStatus.COMPLETED
                """)
        Optional<Contents> findByMediaIdAndStatusAndMedia_PublicStatus(
                @Param("mediaId") Long mediaId,
                @Param("status") Status status,
                @Param("publicStatus") PublicStatus publicStatus);


                
        boolean existsByIdAndStatus(Long id, Status status);
}
