package com.ott.domain.media.repository;

import com.ott.domain.media.domain.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MediaRepository extends JpaRepository<Media, Long>, MediaRepositoryCustom {

    @Query("SELECT m FROM Media m " +
           "WHERE m.status = :status " +
           "AND LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           // 1. 숏폼 제외: 미디어 타입이 'SERIES' 또는 'CONTENTS'인 것만 조회
           "AND m.mediaType IN ('SERIES', 'CONTENTS') " +
           // 2. 에피소드 제외: Contents 테이블에서 series_id가 존재하는 미디어 ID는 제외 (단편만 남음)
           "AND m.id NOT IN (" +
           "    SELECT c.media.id FROM Contents c WHERE c.series IS NOT NULL" +
           ")")
    Page<Media> searchMedia(
            @Param("keyword") String keyword, 
            @Param("status") Status status, 
            Pageable pageable
    );
}
