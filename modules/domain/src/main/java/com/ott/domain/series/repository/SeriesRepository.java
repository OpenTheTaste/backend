package com.ott.domain.series.repository;

import com.ott.domain.series.domain.Series;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series, Long> {

    Page<Series> findByTitleContaining(String keyword, Pageable pageable);
}
