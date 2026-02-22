package com.ott.domain.series.repository;

import com.ott.domain.series.domain.Series;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SeriesRepositoryCustom {

    Page<Series> findSeriesListWithMediaBySearchWord(Pageable pageable, String searchWord);

    Optional<Series> findWithMediaAndUploaderByMediaId(Long mediaId);
}
