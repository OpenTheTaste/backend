package com.ott.api_user.playlist.controller;

import com.ott.api_user.playlist.dto.response.PlaylistResponse;
import com.ott.api_user.playlist.dto.response.TopTagPlaylistResponse;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import com.ott.common.web.exception.ErrorResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Playlist", description = "플레이리스트 API")
public interface PlaylistApi {

    @ApiResponses(value = {
        @ApiResponse(responseCode = "0", description = "조회 성공 - 플레이리스트 구성", content ={
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PlaylistResponse.class))) }),
        @ApiResponse(responseCode = "200", description = "플레이리스트 조회 성공", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)) }),
        @ApiResponse(responseCode = "400", description = "요청 파라미터 오류", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)) })
    })
    
    @Operation(summary = "OO 님이 좋아하실만한 콘텐츠", description = "유저 취향을 합산하여 추천합니다. (홈 화면 셔플 지원)")
    @GetMapping("/recommend")
    ResponseEntity<SuccessResponse<PageResponse<PlaylistResponse>>> getRecommendPlaylists(
            @Parameter(description = "현재 영상 ID") @RequestParam(value = "excludeMediaId", required = false) Long excludeMediaId,
            @Parameter(description = "페이지 번호") @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    );

    @Operation(summary = "선호 태그 순위별 리스트", description = "유저의 Top 3 태그 순위를 기반으로 제공합니다.")
    @GetMapping("/tags/top")
    ResponseEntity<SuccessResponse<TopTagPlaylistResponse>> getTopTagPlaylists(
            @Parameter(description = "현재 영상 ID") @RequestParam(value = "excludeMediaId", required = false) Long excludeMediaId,
            @Parameter(description = "유저 취향 순위 (0, 1, 2)", required = true) @RequestParam(value = "index") Integer index,
            @Parameter(description = "페이지 번호") @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    );

    @Operation(summary = "상세 페이지 - 특정 해시태그 리스트", description = "해당 태그의 영상만 제공합니다.")
    @GetMapping("/tags/{tagId}")
    ResponseEntity<SuccessResponse<PageResponse<PlaylistResponse>>> getTagPlaylists(
            @Parameter(description = "태그 ID", required = true) @PathVariable(value = "tagId") Long tagId,
            @Parameter(description = "현재 영상 ID") @RequestParam(value = "excludeMediaId", required = false) Long excludeMediaId,
            @Parameter(description = "페이지 번호") @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    );

    @Operation(summary = "인기 차트 (Trending)", description = "북마크가 많은 인기 순서대로 제공합니다.")
    @GetMapping("/trending")
    ResponseEntity<SuccessResponse<PageResponse<PlaylistResponse>>> getTrendingPlaylists(
            @Parameter(description = "현재 영상 ID") @RequestParam(value = "excludeMediaId", required = false) Long excludeMediaId,
            @Parameter(description = "페이지 번호") @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    );

    @Operation(summary = "시청 이력 (History)", description = "유저가 최근 시청한 영상 목록을 제공합니다.")
    @GetMapping("/history")
    ResponseEntity<SuccessResponse<PageResponse<PlaylistResponse>>> getHistoryPlaylists(
            @Parameter(description = "현재 영상 ID") @RequestParam(value = "excludeMediaId", required = false) Long excludeMediaId,
            @Parameter(description = "페이지 번호") @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    );

    @Operation(summary = "북마크 목록 (Bookmark)", description = "유저가 북마크한 영상 목록을 제공합니다.")
    @GetMapping("/bookmarks")
    ResponseEntity<SuccessResponse<PageResponse<PlaylistResponse>>> getBookmarkPlaylists(
            @Parameter(description = "현재 영상 ID") @RequestParam(value = "excludeMediaId", required = false) Long excludeMediaId,
            @Parameter(description = "페이지 번호") @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    );
}