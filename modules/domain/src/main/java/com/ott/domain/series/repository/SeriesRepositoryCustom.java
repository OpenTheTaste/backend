package com.ott.domain.series.repository;

import com.ott.domain.series.domain.Series;

import java.util.Optional;

public interface SeriesRepositoryCustom {

    Optional<Series> findWithMediaAndUploaderByMediaId(Long mediaId);
}
