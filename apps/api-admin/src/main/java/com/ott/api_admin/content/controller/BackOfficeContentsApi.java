package com.ott.api_admin.content.controller;

import com.ott.api_admin.content.dto.request.ContentsUploadRequest;
import com.ott.api_admin.content.dto.response.ContentsDetailResponse;
import com.ott.api_admin.content.dto.response.ContentsListResponse;
import com.ott.api_admin.content.dto.response.ContentsUploadResponse;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import com.ott.domain.common.PublicStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
@Tag(name = "BackOffice Contents API", description = "[백오피스] 콘텐츠 관리 API")
public interface BackOfficeContentsApi {

    @Operation(summary = "콘텐츠 목록 조회", description = "콘텐츠 목록을 페이징으로 조회합니다. - ADMIN 권한 필요.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "0", description = "조회 성공 - 페이징 dataList 구성",
                    content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ContentsListResponse.class)))}
            ),
            @ApiResponse(
                    responseCode = "200", description = "콘텐츠 목록 조회 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "콘텐츠 목록 조회 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "403", description = "접근 권한 없음 (ADMIN만 접근 가능)",
                    content = {@Content(mediaType = "application/json")}
            )
    })
    ResponseEntity<SuccessResponse<PageResponse<ContentsListResponse>>> getContents(
            @Parameter(description = "조회할 페이지의 번호를 입력해주세요. **page는 0부터 시작합니다**", required = true) @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "한 페이지 당 최대 항목 개수를 입력해주세요. 기본값은 10입니다.", required = true) @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(description = "제목 부분일치 검색어. 미입력 시 전체 목록을 조회합니다.", required = false) @RequestParam(value = "searchWord", required = false) String searchWord,
            @Parameter(description = "공개 여부. 공개/비공개로 나뉩니다.", required = false, example = "PUBLIC") @RequestParam(value = "publicStatus", required = false) PublicStatus publicStatus
    );

    @Operation(summary = "콘텐츠 상세 조회", description = "콘텐츠 상세 정보를 조회합니다. - ADMIN 권한 필요.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "콘텐츠 상세 조회 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ContentsDetailResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "콘텐츠 상세 조회 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "403", description = "접근 권한 없음 (ADMIN만 접근 가능)",
                    content = {@Content(mediaType = "application/json")}
            )
    })
    ResponseEntity<SuccessResponse<ContentsDetailResponse>> getContentsDetail(
            @Parameter(description = "조회할 콘텐츠의 미디어 ID", required = true) @PathVariable Long mediaId
    );

    @Operation(summary = "콘텐츠 메타데이터 업로드", description = "콘텐츠 메타데이터를 생성하고 S3 업로드용 Presigned URL을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "콘텐츠 메타데이터 업로드 및 Presigned URL 성공",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ContentsUploadResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "콘텐츠 메타데이터 업로드 및 Presigned URL 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "403", description = "접근 권한 없음 (ADMIN만 접근 가능)",
                    content = {@Content(mediaType = "application/json")}
            )
    })
    ResponseEntity<SuccessResponse<ContentsUploadResponse>> createContentsUpload(
            @RequestBody ContentsUploadRequest request
    );
}
