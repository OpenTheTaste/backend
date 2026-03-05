package com.ott.api_user.playback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;

import com.ott.api_user.history.dto.request.WatchHistoryRequest;
import com.ott.api_user.playback.dto.request.PlaybackUpdateRequest;
import com.ott.common.web.response.SuccessResponse;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Validated
@Tag(name = "PlayBack", description = "이어보기 갱신 및 조회 API")
public interface PlayBackApi {
    @Operation(summary = "이어보기 위치 생성 및 갱신", description = "영상 재생 중 이어보기 위치(positionSec)를 저장/갱신합니다.")
    @ApiResponse(responseCode = "200", description = "이어보기 위치 저장 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (mediaId 누락 등)") 
    @ApiResponse(responseCode = "404", description = "존재하지 않는 미디어 ID") 
    ResponseEntity<SuccessResponse<Void>> createPlayBack(
             @Parameter(hidden = true) @AuthenticationPrincipal Long memberId, 
            @Valid @RequestBody PlaybackUpdateRequest request 
    );
} 