package com.ott.domain.playback.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ott.domain.common.Status;
import com.ott.domain.playback.domain.Playback;
import org.springframework.data.jpa.repository.Modifying;

public interface PlaybackRepository extends JpaRepository<Playback, Long> {

    // 회원 탈퇴
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Playback p SET p.status = 'DELETE' WHERE p.member.id = :memberId")
    void softDeleteAllByMemberId(@Param("memberId") Long memberId);


    // 최근 시청한 미디어의 태그 ID 조회
    // 1단계 - 최근 시청 이력 100개 가져오기 (JOIN 보다 LIMIT 먼저)
    @Query("""
            SELECT p.contents.media.id FROM Playback p
            WHERE p.member.id = :memberId AND p.status = :status
            ORDER BY p.modifiedDate DESC
            """)
        List<Long> findRecentPlayedMediaIds(
                @Param("memberId") Long memberId,
                @Param("status") Status status,
                Pageable pageable);

        //현재 플레이리스트의 모든 미디어에 대한 일괄 조회
        @Query("""
                SELECT p FROM Playback p 
                WHERE p.member.id = :memberId 
                AND p.contents.media.id IN :mediaIds
                """)
        List<Playback> findAllByMmberIdAndMediaIds(
            @Param("memberId") Long memberId,
            @Param("mediaIds") List<Long> mediaIds);

        
        // 단건 조회(컨텐츠 or 에피별) 에 대해 시청기록 조회
        @Query("""
                SELECT p FROM Playback p 
                WHERE p.member.id = :memberId 
                AND p.contents.media.id = :mediaId
                """)
        Optional<Playback> findByMemberIdAndMediaId(
                @Param("memberId") Long memberId, 
                @Param("mediaId") Long mediaId);
        
}
