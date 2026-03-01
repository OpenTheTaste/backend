package com.ott.api_user.playlist.service.strategy;

import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import com.ott.api_user.playlist.dto.request.PlaylistCondition;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;

/**
 * 태그별 플레이리스트 전략
 * 특정 태그 ID를 기준으로 관련 미디어 목록을 제공합니다.
 */
@Component("TAG")
@RequiredArgsConstructor
public class TagPlaylistStrategy implements PlaylistStrategy {

    private final MediaRepository mediaRepository;

    @Override
    public Page<Media> getPlaylist(PlaylistCondition condition, Pageable pageable) {
        
        // 1. 핵심 조건인 tagId가 없는 경우 (예외 상황)
        if (condition.getTagId() == null) {
            return mediaRepository.findTrendingPlaylists(condition.getExcludeMediaId(), pageable);
        }

        // 2. 태그 ID가 있다면 홈 화면이든 상세 페이지든 해당 태그 리스트를 반환합니다.
        // 상세 페이지라면 리포지토리 내부 로직에 의해 excludeMediaId가 제외 처리됩니다.
        return mediaRepository.findPlaylistsByTag(
                condition.getTagId(), 
                condition.getExcludeMediaId(), 
                pageable
        );
    }
}