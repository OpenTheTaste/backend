package com.ott.api_admin.category.controller;

import com.ott.api_admin.category.dto.response.CategoryListResponse;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "BackOffice Category API", description = "[백오피스] 카테고리 관리 API")
public interface BackOfficeCategoryApi {

    @Operation(summary = "카테고리 목록 조회", description = "전체 카테고리 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "카테고리 목록 조회 성공",
                    content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CategoryListResponse.class)))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "카테고리 목록 조회 실패",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    ResponseEntity<SuccessResponse<List<CategoryListResponse>>> getCategoryList();
}
