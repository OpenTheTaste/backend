package com.ott.api_user.playlist.service;

import com.ott.api_user.common.ContentSource;
import com.ott.api_user.playlist.dto.request.PlaylistCondition;
import com.ott.api_user.playlist.dto.response.PlaylistResponse;
import com.ott.api_user.playlist.service.strategy.PlaylistStrategy;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.media.domain.Media;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaylistService {

    private final Map<String, PlaylistStrategy> strategyMap;

    public PageResponse<PlaylistResponse> getPlaylists(PlaylistCondition condition, Pageable pageable) {
        
        if (condition.getContentSource() == null) {
        throw new BusinessException(ErrorCode.INVALID_PLAYLIST_SOURCE); 
    }

        // 1. 전략 선택 
        PlaylistStrategy strategy = getStrategy(condition);

        // 2. 데이터 조회
        Page<Media> mediaPage = strategy.getPlaylist(condition, pageable);

        // 3. Entity -> DTO 변환
        List<PlaylistResponse> contentList = mediaPage.getContent().stream()
                .map(PlaylistResponse::from)
                .toList();

        // 4. PageInfo 생성 
        PageInfo pageInfo = PageInfo.toPageInfo(
                mediaPage.getNumber(), 
                mediaPage.getTotalPages(), 
                (int) mediaPage.getTotalElements()
        );

        return PageResponse.toPageResponse(pageInfo, contentList);
    }



    private PlaylistStrategy getStrategy(PlaylistCondition condition) {
        String strategyKey = determineStrategyKey(condition);
        PlaylistStrategy strategy = strategyMap.get(strategyKey);

        if (strategy == null) {
            strategy = strategyMap.get(ContentSource.RECOMMEND.name());
        }
        
        // 여전히 null이라면 시스템 설정 오류이므로 S001 에러 발생
        if (strategy == null) {
            throw new BusinessException(ErrorCode.STRATEGY_NOT_FOUND);
        }
        return strategy;
    }



    private String determineStrategyKey(PlaylistCondition condition) {
        ContentSource source = condition.getContentSource();

        // 검색 결과에서 상세로 진입한 시 재생목록은 추천으로 대체
        if (source == ContentSource.SEARCH && condition.getExcludeMediaId() != null) {
            return ContentSource.RECOMMEND.name();
        }
        return source.name();
    }
}