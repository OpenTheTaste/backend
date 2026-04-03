package com.ott.api_user.playlist.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;

import com.ott.api_user.common.ContentSource;
import com.ott.api_user.playlist.dto.request.PlaylistCondition;
import com.ott.api_user.playlist.dto.response.PlaylistResponse;
import com.ott.api_user.playlist.dto.response.TopTagPlaylistResponse;
import com.ott.api_user.playlist.service.PlaylistStrategyService;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/playlists")
public class PlaylistController implements PlayListAPI {

    private final PlaylistStrategyService playlistStrategytService;

    // 1. 종합 추천
    @Override
    public ResponseEntity<SuccessResponse<PageResponse<PlaylistResponse>>> getRecommendPlaylists(
            @RequestParam(value = "excludeMediaId", required = false) Long excludeMediaId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @AuthenticationPrincipal Long memberId) {

        PlaylistCondition condition = new PlaylistCondition();
        condition.setContentSource(ContentSource.RECOMMEND);
        condition.setExcludeMediaId(excludeMediaId);

        return execute(condition, page, size, memberId);
    }

    // 2. Top 3 태그별 리스트
    @Override
    public ResponseEntity<SuccessResponse<TopTagPlaylistResponse>> getTopTagPlaylists(
            @RequestParam(value = "excludeMediaId", required = false) Long excludeMediaId,
            @RequestParam(value = "index") Integer index,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @AuthenticationPrincipal Long memberId) {

        PlaylistCondition condition = new PlaylistCondition();
        condition.setContentSource(ContentSource.TAG);
        condition.setIndex(index);
        condition.setExcludeMediaId(excludeMediaId);

        if (memberId != null) {
            condition.setMemberId(memberId);
        }

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(SuccessResponse.of(playlistStrategytService.getTopTagPlaylistWithMetadata(condition, pageable)));
        
    }

    // 3. 특정 태그 단건 리스트
    @Override
    public ResponseEntity<SuccessResponse<PageResponse<PlaylistResponse>>> getTagPlaylists(
            @PathVariable(value = "tagId") Long tagId,
            @RequestParam(value = "excludeMediaId", required = false) Long excludeMediaId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @AuthenticationPrincipal Long memberId) {

        PlaylistCondition condition = new PlaylistCondition();
        condition.setContentSource(ContentSource.TAG);
        condition.setTagId(tagId);
        condition.setExcludeMediaId(excludeMediaId);

        return execute(condition, page, size, memberId);
    }

    // 4. 인기 차트
    @Override
    public ResponseEntity<SuccessResponse<PageResponse<PlaylistResponse>>> getTrendingPlaylists(
            @RequestParam(value = "excludeMediaId", required = false) Long excludeMediaId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @AuthenticationPrincipal Long memberId) {

        PlaylistCondition condition = new PlaylistCondition();
        condition.setContentSource(ContentSource.TRENDING);
        condition.setExcludeMediaId(excludeMediaId);

        return execute(condition, page, size, memberId);
    }



    // 5. 시청 이력
    @Override
    public ResponseEntity<SuccessResponse<PageResponse<PlaylistResponse>>> getHistoryPlaylists(
            @RequestParam(value = "excludeMediaId", required = false) Long excludeMediaId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @AuthenticationPrincipal Long memberId) {

        PlaylistCondition condition = new PlaylistCondition();
        condition.setContentSource(ContentSource.HISTORY);
        condition.setExcludeMediaId(excludeMediaId);

        return execute(condition, page, size, memberId);
    }

    // 6. 북마크
    @Override
    public ResponseEntity<SuccessResponse<PageResponse<PlaylistResponse>>> getBookmarkPlaylists(
            @RequestParam(value = "excludeMediaId", required = false) Long excludeMediaId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @AuthenticationPrincipal Long memberId) {

        PlaylistCondition condition = new PlaylistCondition();
        condition.setContentSource(ContentSource.BOOKMARK);
        condition.setExcludeMediaId(excludeMediaId);

        return execute(condition, page, size, memberId);
    }

    // 8. 검색 페이지에서 진입
    @Override
    public ResponseEntity<SuccessResponse<PageResponse<PlaylistResponse>>> getSearchPlaylists(
            @RequestParam(value = "excludeMediaId") Long excludeMediaId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @AuthenticationPrincipal Long memberId) {

        PlaylistCondition condition = new PlaylistCondition();
        condition.setContentSource(ContentSource.SEARCH);
        condition.setExcludeMediaId(excludeMediaId);

        // 서비스단에서 RECOMMEND로 우회됨!
        return execute(condition, page, size, memberId);
    }

        // // 태그 별 추천 리스트 조회
    // @Override
    // @GetMapping("/me/{tagId}")
    // public ResponseEntity<SuccessResponse<List<TagPlaylistResponse>>> getRecommendContentsByTag(
    //         @AuthenticationPrincipal Long memberId,
    //         @Positive @PathVariable Long tagId
    // ) {
    //     return ResponseEntity.ok(SuccessResponse.of(playlistService.getRecommendContentsByTag(memberId, tagId)));
    // }


    // // 과거 시청 이력 조회, 10개씩 조회
    // @Override
    // @GetMapping("/me/history")
    // public ResponseEntity<SuccessResponse<PageResponse<RecentWatchResponse>>> getWatchHistoryPlaylist(
    //         @AuthenticationPrincipal Long memberId,
    //         @PositiveOrZero @RequestParam(defaultValue = "0") Integer page
    // ) {
    //     return ResponseEntity.ok(SuccessResponse.of(playlistService.getWatchHistoryPlaylist(memberId, page)));

    // }


    // 공통 응답 메서드
    private ResponseEntity<SuccessResponse<PageResponse<PlaylistResponse>>> execute(
            PlaylistCondition condition, Integer pageParam, Integer sizeParam, Long memberId) {

        if (memberId != null) {
            condition.setMemberId(memberId);
        }

        Pageable pageable = PageRequest.of(pageParam, sizeParam);

        return ResponseEntity.ok(SuccessResponse.of(playlistStrategytService.getPlaylists(condition, pageable)));
    }
}

