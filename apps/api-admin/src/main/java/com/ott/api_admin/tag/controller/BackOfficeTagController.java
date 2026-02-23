package com.ott.api_admin.tag.controller;

import com.ott.api_admin.tag.dto.response.TagViewResponse;
import com.ott.api_admin.tag.service.BackOfficeTagService;
import com.ott.common.web.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/back-office/admin/tags")
@RequiredArgsConstructor
public class BackOfficeTagController implements BackOfficeTagApi {

    private final BackOfficeTagService backOfficeTagService;

    @Override
    @GetMapping("/stats/{categoryId}")
    public ResponseEntity<SuccessResponse<List<TagViewResponse>>> getTagViewStats(
            @PathVariable(value = "categoryId") Long categoryId
    ) {
        return ResponseEntity.ok(
                SuccessResponse.of(backOfficeTagService.getTagViewStats(categoryId))
        );
    }
}
