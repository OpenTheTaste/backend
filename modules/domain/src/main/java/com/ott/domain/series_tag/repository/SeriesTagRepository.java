package com.ott.domain.series_tag.repository;

import com.ott.domain.series_tag.domain.SeriesTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeriesTagRepository extends JpaRepository<SeriesTag, Long> {

    @Query("SELECT st FROM SeriesTag st "
            + "JOIN FETCH st.tag t "
            + "JOIN FETCH t.category "
            + "WHERE st.series.id IN :seriesIds")
    List<SeriesTag> findWithTagAndCategoryBySeriesIds(@Param("seriesIds") List<Long> seriesIds);
}
