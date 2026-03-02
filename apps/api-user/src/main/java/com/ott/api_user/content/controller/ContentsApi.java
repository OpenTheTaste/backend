package com.ott.api_user.content.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.ott.api_user.common.ContentSource;
import com.ott.api_user.content.dto.ContentsDetailResponse;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Contents", description = "콘텐츠 상세 및 재생 관련 API")
public interface ContentsApi {

        @Operation(summary = "콘텐츠 상세 조회", description = "단편 영화/에피소드의 상세 정보를 조회합니다.(콘텐츠 상세 페이지)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "0", description = "조회 성공 - 콘텐츠 상세 구성", content = {
                                        @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ContentsDetailResponse.class))) }),
                        @ApiResponse(responseCode = "200", description = "콘텐츠 상세 조회 성공", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = ContentsDetailResponse.class)) }),
                        @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)) })
        })
        @GetMapping("/{mediaId}")
        ResponseEntity<SuccessResponse<ContentsDetailResponse>> getContentDetail(
                        @Parameter(description = "미디어 ID", required = true, example = "1") @PathVariable("mediaId") Long mediaId,
                        @Parameter(hidden = true) @AuthenticationPrincipal Long memberId);

}
