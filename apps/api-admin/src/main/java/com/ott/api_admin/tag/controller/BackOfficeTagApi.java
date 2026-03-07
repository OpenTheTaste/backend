package com.ott.api_admin.tag.controller;

import com.ott.api_admin.tag.dto.response.TagResponse;
import com.ott.api_admin.tag.dto.response.TagViewResponse;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "BackOffice Tag API", description = "[백오피스] 태그 시청 통계 API")
public interface BackOfficeTagApi {

    @Operation(summary = "카테고리별 태그 당월 시청 통계 조회", description = "특정 카테고리에 속한 태그들의 당월 시청 수를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "태그 시청 통계 조회 성공",
                    content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TagViewResponse.class)))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "태그 시청 통계 조회 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    ResponseEntity<SuccessResponse<List<TagViewResponse>>> getTagViewStats(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable(value = "categoryId") Long categoryId
    );

    @Operation(summary = "카테고리별 태그 목록 조회", description = "특정 카테고리에 속한 태그 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "카테고리별 태그 목록 조회 성공",
                    content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TagResponse.class)))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "카테고리별 태그 목록 조회 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    ResponseEntity<SuccessResponse<List<TagResponse>>> getTagListByCategory(
            @Positive @Parameter(description = "카테고리 ID", example = "1") Long categoryId
    );
}
