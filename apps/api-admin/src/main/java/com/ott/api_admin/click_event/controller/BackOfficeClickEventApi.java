package com.ott.api_admin.click_event.controller;

import com.ott.api_admin.click_event.dto.response.ShortFormConversionResponse;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "BackOffice Click-Event API", description = "[백오피스] 클릭 이벤트 통계 API")
public interface BackOfficeClickEventApi {

    @Operation(summary = "숏폼 -> 콘텐츠 전환율 조회", description = "이번 달 숏폼 전환율 및 전월 대비 증감율을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "숏폼 -> 콘텐츠 전환율 조회 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ShortFormConversionResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "숏폼 -> 콘텐츠 전환율 조회 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    ResponseEntity<SuccessResponse<ShortFormConversionResponse>> getShortFormConversionStats();
}
