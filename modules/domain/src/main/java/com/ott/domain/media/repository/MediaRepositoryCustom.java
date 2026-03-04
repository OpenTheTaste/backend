package com.ott.domain.media.repository;

import com.ott.domain.common.MediaType;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.media.domain.Media;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface MediaRepositoryCustom {

        Page<Media> findMediaListByMediaTypeAndSearchWord(Pageable pageable, MediaType mediaType, String searchWord);

        Page<Media> findMediaListByMediaTypeAndSearchWordAndPublicStatus(Pageable pageable, MediaType mediaType,
                        String searchWord, PublicStatus publicStatus);

        Page<Media> findMediaListByMediaTypeAndSearchWordAndPublicStatusAndUploaderId(Pageable pageable,
                        MediaType mediaType, String searchWord, PublicStatus publicStatus, Long uploaderId);

        Page<Media> findOriginMediaListBySearchWord(Pageable pageable, String searchWord);

        List<TagContentProjection> findRecommendContentsByTagId(Long tagId, int limit);

        // 인기 차트 통합 조회 메서드
        Page<Media> findTrendingPlaylists(Long excludeMediaId, Pageable pageable);

        // 시청 이력 조회 (최근 시청 순)
        Page<Media> findHistoryPlaylists(Long memberId, Long excludeMediaId, Pageable pageable);

        // 북마크 목록 조회 (최근 찜한 순)
        Page<Media> findBookmarkedPlaylists(Long memberId, Long excludeMediaId, Pageable pageable);

        // 특정 태그 기반 미디어 목록 조회
        Page<Media> findPlaylistsByTag(Long tagId, Long excludeMediaId, Pageable pageable);


        List<Media> findMediasByTagId(Long tagId, Long excludeMediaId, int limit , long offset);

        List<Media> findRecommendedMedias(Map<Long, Integer> tagScores, Long excludeMediaId, int limit, long offset);
}