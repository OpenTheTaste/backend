package com.ott.api_admin.category.mapper;

import com.ott.api_admin.category.dto.response.CategoryListResponse;
import com.ott.domain.category.domain.Category;
import org.springframework.stereotype.Component;

@Component
public class BackOfficeCategoryMapper {

    public CategoryListResponse toCategoryListResponse(Category category) {
        return new CategoryListResponse(
                category.getId(),
                category.getName()
        );
    }
}
