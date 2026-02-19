package com.ott.domain.series.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ott.domain.common.Status;
import com.ott.domain.series.domain.Series;

public interface SeriesRepository extends JpaRepository<Series, Long> {

        Page<Series> findByTitleContaining(String keyword, Pageable pageable);

        // 제목에 검색어 포함, 상태 ACTIVE인 시리즈 검색 (최신순 정렬)
        @Query("SELECT s FROM Series s " +
                        "WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "AND s.status = :status " +
                        "ORDER BY s.createdDate DESC")
        List<Series> searchLatest(@Param("keyword") String keyword,
                        @Param("status") Status status,
                        Pageable pageable);
}
