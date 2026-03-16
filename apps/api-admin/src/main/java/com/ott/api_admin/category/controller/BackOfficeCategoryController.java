package com.ott.api_admin.category.controller;

import com.ott.api_admin.category.dto.response.CategoryListResponse;
import com.ott.api_admin.category.service.BackOfficeCategoryService;
import com.ott.common.web.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/back-office/categories")
@RequiredArgsConstructor
public class BackOfficeCategoryController implements BackOfficeCategoryApi {

    private final BackOfficeCategoryService backOfficeCategoryService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse<List<CategoryListResponse>>> getCategoryList() {
        return ResponseEntity.ok(
                SuccessResponse.of(backOfficeCategoryService.getCategoryList())
        );
    }
}
