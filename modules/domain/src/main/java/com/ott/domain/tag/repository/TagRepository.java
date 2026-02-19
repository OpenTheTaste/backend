package com.ott.domain.tag.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ott.domain.tag.domain.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

    @Query("SELECT t.name FROM Tag t" +
            "JOIN SeriesTag st ON t.id = st.tagId" +
            "WHERE st.seriesId = :seriesId" +
            "AND t.status = 'ACTIVE'")
    List<String> findTagNamesBySeriesId(@Param("seriesId") Long seriesId);
}