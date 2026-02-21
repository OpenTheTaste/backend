package com.ott.api_admin.content.controller;

import com.ott.api_admin.content.dto.response.ContentsListResponse;
import com.ott.api_admin.content.service.BackOfficeContentsService;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import com.ott.domain.common.PublicStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/back-office")
@RequiredArgsConstructor
public class BackOfficeContentsController implements BackOfficeContentsApi {

    private final BackOfficeContentsService backOfficeContentsService;

    @Override
    @GetMapping("/admin/contents")
    public ResponseEntity<SuccessResponse<PageResponse<ContentsListResponse>>> getContents(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "searchWord", required = false) String searchWord,
            @RequestParam(value = "publicStatus", required = false) PublicStatus publicStatus
    ) {
        return ResponseEntity.ok(
                SuccessResponse.of(backOfficeContentsService.getContents(page, size, searchWord, publicStatus))
        );
    }
}
