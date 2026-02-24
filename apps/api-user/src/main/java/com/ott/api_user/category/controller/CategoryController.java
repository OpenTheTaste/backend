package com.ott.api_user.category.controller;

import com.ott.api_user.category.dto.response.CategoryResponse;
import com.ott.api_user.category.service.CategoryService;
import com.ott.common.web.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController implements CategoryApi {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(SuccessResponse.of(categoryService.getCategories()));
    }
}
