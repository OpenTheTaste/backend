package com.ott.api_admin.series.controller;

import com.ott.api_admin.series.dto.response.SeriesListResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "BackOffice Series API", description = "[백오피스] 시리즈 관리 API")
public interface BackOfficeSeriesApi {

    @Operation(summary = "시리즈 목록 조회", description = "시리즈 목록을 페이징으로 조회합니다. - ADMIN 권한 필요.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "시리즈 목록 조회 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SeriesListResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "시리즈 목록 조회 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "403", description = "접근 권한 없음 (ADMIN만 접근 가능)",
                    content = {@Content(mediaType = "application/json")}
            )
    })
    ResponseEntity<SuccessResponse<PageResponse>> getSeries(
            @Parameter(description = "조회할 페이지의 번호를 입력해주세요. **page는 0부터 시작합니다**", required = true) @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "한 페이지 당 최대 항목 개수를 입력해주세요. 기본값은 10입니다.", required = true) @RequestParam(value = "size", defaultValue = "10") Integer size
    );
}
