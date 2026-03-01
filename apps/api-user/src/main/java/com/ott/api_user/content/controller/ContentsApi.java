package com.ott.api_user.content.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.ott.api_user.common.ContentSource;
import com.ott.api_user.common.dto.ContentListElement;
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
        @GetMapping("/{contentsId}")
        ResponseEntity<SuccessResponse<ContentsDetailResponse>> getContentDetail(
                        @Parameter(description = "콘텐츠 ID", required = true, example = "1") @PathVariable("contentsId") Long contentsId,
                        @Parameter(hidden = true) @AuthenticationPrincipal Long memberId);

        // // 맥락 (진입점) 기반 플레이리스트 조회 - 해당 API 는 홈화면의 플레이리스트 조회 API 와 별도로 작성한다?? 아니면 재사용
        // @Operation(summary = "진입점 기반 재생 목록 조회", description = "재생 화면 하단/우측에 노출되는 맞춤형 추천 리스트(재생 목록)를 조회합니다.")
        // @ApiResponses(value = {
        //                 @ApiResponse(responseCode = "0", description = "조회 성공 - 재생 목록 구성", content = {
        //                                 @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ContentListElement.class))) }),
        //                 @ApiResponse(responseCode = "200", description = "플레이리스트 조회 성공", content = {
        //                                 @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)) }),
        //                 @ApiResponse(responseCode = "400", description = "요청 파라미터 오류", content = {
        //                                 @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)) })
        // })
        // @GetMapping("/{contentsId}/playlist")
        // ResponseEntity<SuccessResponse<PageResponse<ContentListElement>>> getContentPlayList(
        //                 @Parameter(description = "현재 재생 중인 콘텐츠 ID", required = true, example = "10") @PathVariable("contentsId") Long contentsId,
        //                 @Parameter(description = "진입 맥락 (TRENDING, HISTORY, TAG 등)", example = "TAG") @RequestParam(value = "source", required = false) ContentSource source,
        //                 @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(value = "page", defaultValue = "0") Integer pageParam,
        //                 @Parameter(description = "페이지 크기", example = "20") @RequestParam(value = "size", defaultValue = "20") Integer sizeParam,
        //                 @Parameter(hidden = true) @AuthenticationPrincipal Long memberId);
}
