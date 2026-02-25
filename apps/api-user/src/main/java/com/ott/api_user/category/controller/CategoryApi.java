package com.ott.api_user.category.controller;

import com.ott.api_user.category.dto.response.CategoryResponse;
import com.ott.api_user.tag.dto.response.TagResponse;
import com.ott.common.web.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Category", description = "카테고리 API")
@SecurityRequirement(name = "BearerAuth")
public interface CategoryApi {

    @Operation(summary = "카테고리 목록 조회", description = "전체 카테고리 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryResponse.class)
                    )
            )
    })
    ResponseEntity<SuccessResponse<List<CategoryResponse>>> getCategories();


    @Operation(summary = "카테고리별 태그 목록 조회", description = "특정 카테고리에 속한 태그 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TagResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "카테고리를 찾을 수 없음",
                    content = @Content(mediaType = "application/json")
            )
    })
    ResponseEntity<SuccessResponse<List<TagResponse>>> getTagsByCategory(
            @Parameter(description = "카테고리 ID", example = "1") Long categoryId
    );
}