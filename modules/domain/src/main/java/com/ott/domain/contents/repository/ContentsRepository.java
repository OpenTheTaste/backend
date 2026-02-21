package com.ott.domain.contents.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ott.domain.common.PublicStatus;
import com.ott.domain.common.Status;
import com.ott.domain.contents.domain.Contents;

public interface ContentsRepository extends JpaRepository<Contents, Long> {

        // 제목에 검색어 포함, 상태 ACTIVE, 시리즈 없는 콘텐츠만 검색 (최신순 정렬)
        @Query("SELECT c FROM Contents c " +
                        "WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "AND c.status = :status " +
                        "AND c.series IS NULL " +
                        "ORDER BY c.createdDate DESC")
        List<Contents> searchLatest(@Param("keyword") String searchWord, @Param("status") Status status,
                        Pageable pageable);

        // 특정 시리즈의 콘텐츠(에피소드) 들을 1화부터 오름차순으로 페이징해서 가져옴.
        Page<Contents> findBySeriesIdAndStatusAndPublicStatusOrderByIdAsc(Long seriesId, Status status,
                        PublicStatus publicStatus, Pageable pageable);

}