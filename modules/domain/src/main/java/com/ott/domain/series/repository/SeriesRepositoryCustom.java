package com.ott.domain.series.repository;

import com.ott.domain.series.domain.Series;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SeriesRepositoryCustom {

    Page<Series> findSeriesList(Pageable pageable, String keyword);
}
