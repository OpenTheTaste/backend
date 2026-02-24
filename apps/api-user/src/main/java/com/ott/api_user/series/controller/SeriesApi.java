package com.ott.api_user.series.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.ott.api_user.series.dto.SeriesContentsResponse;
import com.ott.api_user.series.dto.SeriesDetailResponse;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Series API", description = "시리즈 관련 API입니다.")
public interface SeriesApi {
        @Operation(summary = "시리즈 상세 조회", description = "특정 시리즈의 상세 정보를 조회합니다.(시리즈 상세 페이지)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "시리즈 상세 조회 성공", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = SeriesDetailResponse.class)) }),
                        @ApiResponse(responseCode = "404", description = "시리즈를 찾을 수 없음", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)) })
        })
        @GetMapping("/{seriesId}")
        ResponseEntity<SuccessResponse<SeriesDetailResponse>> getSeriesDetail(
                        @Parameter(description = "시리즈 ID", required = true, example = "1") @PathVariable("seriesId") Long seriesId,
                        @Parameter(hidden = true) Long memberId // 토큰에서 추출 (스웨거에서는 숨김)
        );

        @Operation(summary = "시리즈 콘텐츠 목록 조회", description = "특정 시리즈에 속한 콘텐츠(에피소드) 목록을 페이징하여 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "시리즈 콘텐츠 목록 조회 성공", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)) }),
                        @ApiResponse(responseCode = "404", description = "시리즈를 찾을 수 없음", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)) })
        })
        @GetMapping("/{seriesId}/contents")
        ResponseEntity<SuccessResponse<PageResponse<SeriesContentsResponse>>> getSeriesContents(
                        @Parameter(description = "시리즈 ID", required = true) @PathVariable("seriesId") Long seriesId,
                        @Parameter(description = "페이지 번호 (0부터 시작)", schema = @Schema(defaultValue = "0")) @RequestParam("page") Integer page,
                        @Parameter(description = "페이지 크기", schema = @Schema(defaultValue = "24")) @RequestParam("size") Integer size,
                        @Parameter(hidden = true) Long memberId // 토큰에서 추출 (스웨거에서는 숨김)
        );
        // 이어보기 지점 추가
}