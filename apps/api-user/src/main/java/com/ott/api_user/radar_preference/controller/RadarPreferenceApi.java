package com.ott.api_user.radar_preference.controller;

import com.ott.api_user.radar_preference.dto.request.RadarPreferenceRequest;
import com.ott.api_user.radar_preference.dto.response.RadarMediaResponse;
import com.ott.api_user.radar_preference.dto.response.RadarPreferenceResponse;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Radar", description = "레이더 차트 API")
public interface RadarPreferenceApi {

    @Operation(summary = "레이더 차트 설정 조회", description = "사용자의 레이더 차트 슬라이더 설정값을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레이더 차트 설정 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RadarPreferenceResponse.class))),
            @ApiResponse(responseCode = "404", description = "레이더 차트 설정을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    ResponseEntity<SuccessResponse<RadarPreferenceResponse>> getPreference(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    );

    @Operation(summary = "레이더 차트 설정 수정", description = "사용자의 레이더 차트 슬라이더 설정값을 수정합니다. 5개 축 모두 0~100 범위.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "레이더 차트 설정 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (범위 초과 등)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "레이더 차트 설정을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping
    ResponseEntity<Void> updatePreference(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody RadarPreferenceRequest request
    );

    @Operation(summary = "레이더 차트 추천 리스트 조회", description = "사용자의 슬라이더 설정 기반으로 가중합 추천 미디어 상위 20개를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레이더 차트 추천 리스트 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RadarMediaResponse.class))),
            @ApiResponse(responseCode = "404", description = "레이더 차트 설정을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/recommend")
    ResponseEntity<SuccessResponse<List<RadarMediaResponse>>> getRecommendationList(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    );
}
