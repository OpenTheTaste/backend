package com.ott.api_admin.shortform.controller;

import com.ott.api_admin.shortform.dto.ShortFormListResponse;
import com.ott.api_admin.shortform.service.BackOfficeShortFormService;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import com.ott.domain.common.PublicStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/back-office/short-forms")
@RequiredArgsConstructor
public class BackOfficeShortFormController implements BackOfficeShortFormApi {

    private final BackOfficeShortFormService backOfficeShortFormService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse<PageResponse<ShortFormListResponse>>> getShortFormList(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "searchWord", required = false) String searchWord,
            @RequestParam(value = "publicStatus", required = false) PublicStatus publicStatus,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                SuccessResponse.of(backOfficeShortFormService.getShortFormList(page, size, searchWord, publicStatus, authentication))
        );
    }
}
