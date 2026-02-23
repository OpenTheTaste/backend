package com.ott.api_admin.ingest_job.controller;

import com.ott.api_admin.ingest_job.dto.response.IngestJobListResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "BackOffice IngestJob API", description = "[백오피스] 업로드 작업 관리 API")
public interface BackOfficeIngestJobApi {

    @Operation(summary = "업로드 작업 목록 조회", description = "업로드 작업 목록을 페이징으로 조회합니다. ADMIN은 전체, EDITOR는 본인 업로드만 조회됩니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "0", description = "조회 성공 - 페이징 dataList 구성",
                    content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = IngestJobListResponse.class)))}
            ),
            @ApiResponse(
                    responseCode = "200", description = "업로드 작업 목록 조회 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "업로드 작업 목록 조회 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    ResponseEntity<SuccessResponse<PageResponse<IngestJobListResponse>>> getIngestJobList(
            @Parameter(description = "조회할 페이지의 번호를 입력해주세요. **page는 0부터 시작합니다**", required = true) @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "한 페이지 당 최대 항목 개수를 입력해주세요. 기본값은 10입니다.", required = true) @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(description = "콘텐츠 제목 부분일치 검색어. 미입력 시 전체 목록을 조회합니다.", required = false) @RequestParam(value = "searchWord", required = false) String searchWord,
            Authentication authentication
    );
}
