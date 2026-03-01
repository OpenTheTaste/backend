package com.ott.api_user.playlist.controller;

import com.ott.api_user.playlist.dto.request.PlaylistCondition;
import com.ott.api_user.playlist.dto.response.PlaylistResponse;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ott.common.web.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Playlist", description = "플레이리스트 API")
public interface PlaylistApi {
    @Operation(summary = "플레이리스트 조회", description = "source 타입에 따라 동적으로 플레이리스트를 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "0", description = "조회 성공 - 플레이리스트 구성", content ={
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PlaylistResponse.class))) }),
        @ApiResponse(responseCode = "200", description = "플레이리스트 조회 성공", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)) }),
        @ApiResponse(responseCode = "400", description = "요청 파라미터 오류", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)) })
    })
    @GetMapping
    ResponseEntity<SuccessResponse<PageResponse<PlaylistResponse>>> getPlaylists(
                        @Parameter(description = "플레이리스트 조회 조건 (source 필수)", required = true) PlaylistCondition condition,
                        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") Integer page,
                        @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") Integer size,
                        @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    );
}
