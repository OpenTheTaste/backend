package com.ott.api_user.category.dto.response;

import com.ott.domain.category.domain.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "카테고리 응답 DTO")
public class CategoryResponse {

    @Schema(type= "Long", example = "1", description = "카테고리 ID")
    private Long categoryId;

    @Schema(type ="String", example = "영화", description = "카테고리 이름")
    private String name;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getId())
                .name(category.getName())
                .build();
    }
}
