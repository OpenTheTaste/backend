package com.ott.domain.category.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ott.domain.category.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
            SELECT DISTINCT c.name
            FROM MediaTag mt
            JOIN mt.tag t
            JOIN t.category c
            WHERE mt.media.id = :mediaId
              AND mt.status = 'ACTIVE'
              AND t.status = 'ACTIVE'
              AND c.status = 'ACTIVE'
            """)
    List<String> findCategoryNamesByMediaId(@Param("mediaId") Long mediaId);
}