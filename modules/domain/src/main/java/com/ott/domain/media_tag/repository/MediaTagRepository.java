package com.ott.domain.media_tag.repository;

import com.ott.domain.media_tag.domain.MediaTag;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MediaTagRepository extends JpaRepository<MediaTag, Long>, MediaTagRepositoryCustom {

    // 최근 좋아요한 미디어의 태그 ID 조회
    // [2단계] 미디어에 대한 태그들을 전부 가져옴
    @Query("""
            SELECT mt.tag.id FROM MediaTag mt
            WHERE mt.media IN :mediaIds
            """)
    List<Long> findTagIdsByMediaIds(@Param("mediaIds") List<Long> mediaIds);
}
