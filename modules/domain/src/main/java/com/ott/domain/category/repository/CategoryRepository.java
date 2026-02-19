package com.ott.domain.category.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ott.domain.category.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 하나의 시리즈는 하나의 카테고리를 갖지만
    // 일단 List 형태로 처리
    @Query("SELECT DISTINCT c.name FROM Category c" +
            "JOIN Tag t ON c.id = t.category.id" +
            "JOIN SeriesTag st ON t.id = st.tagId" +
            "WHERE st.seriesId = :seriesId" +
            "AND c.status = 'ACTIVE'")
    List<String> findCategoryNameBySeriesId(@Param("seriesId") Long seriesId);
}