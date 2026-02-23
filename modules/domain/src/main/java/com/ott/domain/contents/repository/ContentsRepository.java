package com.ott.domain.contents.repository;

import com.ott.domain.contents.domain.Contents;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentsRepository extends JpaRepository<Contents, Long>, ContentsRepositoryCustom {

        // 제목에 검색어 포함, 상태 ACTIVE, 시리즈 없는 콘텐츠만 검색 (최신순 정렬)
//        @Query("SELECT c FROM Contents c " +
//                        "WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
//                        "AND c.status = :status " +
//                        "AND c.series IS NULL " +
//                        "ORDER BY c.createdDate DESC")
//        List<Contents> searchLatest(@Param("keyword") String searchWord, @Param("status") Status status,
//                        Pageable pageable);

}