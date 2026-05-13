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

public interface PlaybackRepository extends JpaRepository<Playback, Long>, PlaybackRepositoryCustom {

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
                AND p.contents.media.id IN :mediaIdList
                AND p.status = 'ACTIVE'
                ORDER BY p.modifiedDate DESC
                """)
        List<Playback> findAllByMemberIdAndMediaIds(
            @Param("memberId") Long memberId,
            @Param("mediaIdList") List<Long> mediaIdList);

        
        // 단건 조회(컨텐츠 or 에피별) 에 대해 시청기록 조회
        @Query("""
                SELECT p FROM Playback p
                WHERE p.member.id = :memberId
                AND p.contents.media.id = :mediaId
                AND p.status = 'ACTIVE'
                """)
        Optional<Playback> findByMemberIdAndMediaId(
                @Param("memberId") Long memberId,
                @Param("mediaId") Long mediaId);
        
        
        // legacy: contentsId를 이미 조회한 뒤 upsert하는 기존 방식
        @Modifying
        @Query(value = """
                INSERT INTO playback (member_id, contents_id, position_sec, created_date, modified_date, status)
                VALUES (:memberId, :contentsId, :positionSec, NOW(), NOW(), 'ACTIVE')
                ON DUPLICATE KEY UPDATE 
                        position_sec = :positionSec,
                        modified_date = NOW()
                """, nativeQuery = true)
        void upsertPlayback(
                @Param("memberId") Long memberId, 
                @Param("contentsId") Long contentsId, 
                @Param("positionSec") int positionSec);

        // new: mediaId 기반 단일 SQL 경로. affected rows를 반환해 fallback 판단에 사용
        @Modifying
        @Query(value = """
                INSERT INTO playback (member_id, contents_id, position_sec, created_date, modified_date, status)
                SELECT :memberId, c.id, :positionSec, NOW(), NOW(), 'ACTIVE'
                FROM contents c
                JOIN media m ON m.id = c.media_id
                WHERE m.id = :mediaId
                  AND c.status = 'ACTIVE'
                  AND m.public_status = 'PUBLIC'
                  AND m.media_status = 'COMPLETED'
                ON DUPLICATE KEY UPDATE
                    position_sec = :positionSec,
                    modified_date = NOW()
                """, nativeQuery = true)
        int upsertPlaybackByMediaId(
                @Param("memberId") Long memberId,
                @Param("mediaId") Long mediaId,
                @Param("positionSec") int positionSec);
}
