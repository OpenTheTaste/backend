package com.ott.api_user.tag.controller;

import com.ott.api_user.tag.dto.response.TagMonthlyCompareResponse;
import com.ott.api_user.tag.dto.response.TagRankingResponse;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/tag")
@Tag(name = "Tag", description = "태그 API")
@SecurityRequirement(name = "BearerAuth") // 인증인가 확인
public interface TagAPI {

    // -------------------------------------------------------
    // 시청이력 기반 태그 랭킹 조회
    // -------------------------------------------------------
    @Operation(summary = "시청이력 기반 태그 랭킹 조회", description = "최근 1달간 시청이력을 기반으로 상위 4개 태그 + 기타 항목을 반환"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TagRankingResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "회원을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/me/ranking")
    ResponseEntity<SuccessResponse<TagRankingResponse>> getTagRanking(
            @AuthenticationPrincipal Long memberId);


    // -------------------------------------------------------
    // 태그 월별 시청 count 비교
    // -------------------------------------------------------
    @Operation(summary = "태그 월별 시청 count 비교", description = "특정 태그의 이번 달 vs 저번 달 시청 횟수를 반환"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TagMonthlyCompareResponse.class)
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
    @GetMapping("/me/ranking/{tagId}")
    ResponseEntity<SuccessResponse<TagMonthlyCompareResponse>> getTagMonthlyCompare(
            @AuthenticationPrincipal Long memberId,
            @Positive @PathVariable Long tagId
    );
}
