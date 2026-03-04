package com.ott.api_user.playlist.controller;

import com.ott.api_user.playlist.dto.response.RecentWatchResponse;
import com.ott.api_user.playlist.dto.response.TagPlaylistResponse;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/playlists")
@SecurityRequirement(name = "BearerAuth") // 인증인가 확인
@Tag(name = "Playlist", description = "플레이리스트 API")
public interface PlayListAPI {

    // -------------------------------------------------------
    // 태그별 추천 콘텐츠 목록 조회
    // -------------------------------------------------------
    @Operation(summary = "태그별 추천 콘텐츠 리스트 조회", description = "해당 태그에 속하는 콘텐츠를 최대 20개 반환"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TagPlaylistResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "회원 또는 태그를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/me/{tagId}")
    ResponseEntity<SuccessResponse<List<TagPlaylistResponse>>> getRecommendContentsByTag(
            @AuthenticationPrincipal Long memberId,
            @Positive @PathVariable Long tagId
    );


    // -------------------------------------------------------
    // 전체 시청이력 플레이리스트 페이징 조회
    // -------------------------------------------------------
    @Operation(summary = "과거 시청 이력 리스트 조회", description = "전체 시청이력을 최신순으로 10개씩 페이징 조회합니다. 이어보기 시점 포함.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(
                    responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404", description = "회원을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me/history")
    ResponseEntity<SuccessResponse<PageResponse<RecentWatchResponse>>> getWatchHistoryPlaylist(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer page
    );
}
