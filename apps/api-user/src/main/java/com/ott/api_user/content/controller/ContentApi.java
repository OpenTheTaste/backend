package com.ott.api_user.content.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.ott.api_user.content.dto.ContentDetailResponse;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import com.ott.domain.common.ContentSource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Contents", description = "콘텐츠(영상) 상세 및 재생 관련 API")
public interface ContentApi {

    @Operation(summary = "콘텐츠 상세 조회", description = "단편 영화/에피소드의 상세 정보를 조회합니다.(콘텐츠 상세 페이지)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "콘텐츠 상세 조회 성공", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ContentDetailResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)) })
    })
    @GetMapping("/{contentsId}")
    ResponseEntity<SuccessResponse<ContentDetailResponse>> getContentDetail(
            @Parameter(description = "콘텐츠 ID", required = true, example = "1") @PathVariable("contentsId") Long contentsId,
            @Parameter(hidden = true) Long memberId);

    // 맥락 (진입점) 기반 플레이리스트 조회 - 해당 API 는 홈화면의 플레이리스트 조회 API 와 별도로 작성한다?? 아니면 재사용

    // 댓글 조회
}
