package com.ott.api_admin.content.controller;

import com.ott.api_admin.content.dto.request.ContentsUpdateRequest;
import com.ott.api_admin.content.dto.request.ContentsUploadRequest;
import com.ott.api_admin.content.dto.response.ContentsDetailResponse;
import com.ott.api_admin.content.dto.response.ContentsListResponse;
import com.ott.api_admin.content.dto.response.ContentsUpdateResponse;
import com.ott.api_admin.content.dto.response.ContentsUploadResponse;
import com.ott.api_admin.content.service.BackOfficeContentsService;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import com.ott.domain.common.PublicStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/back-office/admin/contents")
@RequiredArgsConstructor
public class BackOfficeContentsController implements BackOfficeContentsApi {

    private final BackOfficeContentsService backOfficeContentsService;

    @Override
    @GetMapping
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

    @Override
    @GetMapping("/{mediaId}")
    public ResponseEntity<SuccessResponse<ContentsDetailResponse>> getContentsDetail(
            @PathVariable("mediaId") Long mediaId
    ) {
        return ResponseEntity.ok(
                SuccessResponse.of(backOfficeContentsService.getContentsDetail(mediaId))
        );
    }

    @Override
    @PostMapping("/upload")
    public ResponseEntity<SuccessResponse<ContentsUploadResponse>> createContentsUpload(
            @Valid @RequestBody ContentsUploadRequest request
    ) {
        return ResponseEntity.ok(SuccessResponse.of(backOfficeContentsService.createContentsUpload(request)));
    }

    @Override
    @PatchMapping("/{mediaId}/upload")
    public ResponseEntity<SuccessResponse<ContentsUpdateResponse>> updateContentsUpload(
            @PathVariable("mediaId") Long mediaId,
            @Valid @RequestBody ContentsUpdateRequest request
    ) {
        return ResponseEntity.ok(SuccessResponse.of(backOfficeContentsService.updateContentsUpload(mediaId, request)));
    }
}