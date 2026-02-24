package com.ott.domain.tag.repository;

import java.util.List;

import com.ott.domain.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ott.domain.common.Status;
import com.ott.domain.tag.domain.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // 시리즈/콘텐츠에 연결된 태그 조회
    @Query("""
            SELECT DISTINCT t.name
            FROM MediaTag mt
            JOIN mt.tag t
            WHERE mt.media.id = :mediaId
            AND t.status = :status
            AND mt.status = :status
            """)
    List<String> findTagNamesByMediaId(@Param("mediaId") Long mediaId, @Param("status") Status status);

    List<Tag> findAllByCategoryAndStatus(Category category, Status status);
}