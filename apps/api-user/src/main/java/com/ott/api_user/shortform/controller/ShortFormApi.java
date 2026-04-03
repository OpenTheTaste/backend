package com.ott.api_user.shortform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ott.api_user.series.dto.SeriesDetailResponse;
import com.ott.api_user.shortform.dto.request.ShortFormEventRequest;
import com.ott.api_user.shortform.dto.response.ShortFormFeedResponse;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Tag(name = "ShortForm", description = "숏폼 피드 및 클릭 이벤트 API")
public interface ShortFormApi {
    // 1. 숏폼 피드 목록 조회 API
    @Operation(summary = "숏폼 피드 목록 조회", description = "무한 스크롤을 위한 숏폼 피드를 반환합니다. (추천 70% + 최신작 30% 혼합 구성)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "조회 성공 - 숏폼 피드 상세 구성", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ShortFormFeedResponse.class)) }),
            @ApiResponse(responseCode = "200", description = "숏폼 피드 조회 성공", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "잘못된 페이징 파라미터", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)) })
    })
    @GetMapping
    ResponseEntity<SuccessResponse<PageResponse<ShortFormFeedResponse>>> getShortFormFeed(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @PositiveOrZero @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") Integer page,
            @Positive @Parameter(description = "패이지 크기", example = "10") @RequestParam(defaultValue = "10") Integer size
    );


    // 2. 숏폼 시청(조회) 이벤트 로깅 API
    @Operation(summary = "숏폼 시청 이벤트 로깅 (5초 체류)", description = "유저가 숏폼에 5초 이상 머물렀을 때 호출하여 통계를 기록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "숏폼 시청 이벤트 저장 성공, 응답 본문 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 숏폼 ID", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)) })
    })
    @PostMapping("/events")
    ResponseEntity<Void> recordShortFormView(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody ShortFormEventRequest request
    );


    // 3. CTA 클릭 이벤트 API
    @Operation(summary = "숏폼 CTA 클릭 이벤트 로깅", description = "유저가 숏폼에서 '본편 보러가기' 버튼을 클릭했을 때 호출하여 통계를 기록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "CTA 클릭 이벤트 저장 성공, 응답 본문 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 숏폼 ID", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)) })
    })
    @PostMapping("/cta")
    ResponseEntity<Void> recordCtaClick(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody ShortFormEventRequest request
    );

}