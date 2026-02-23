package com.ott.api_admin.upload.controller;

import com.ott.api_admin.upload.dto.request.ContentsUploadInitRequest;
import com.ott.api_admin.upload.dto.response.ContentsUploadInitResponse;
import com.ott.api_admin.upload.service.ContentsUploadService;
import com.ott.common.web.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 콘텐츠 업로드 초기화 요청을 처리하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/back-office/admin/upload/contents")
@RequiredArgsConstructor
public class ContentsUploadController implements ContentsUploadApi {

    private final ContentsUploadService contentsUploadService;

    @Override
    @PostMapping("/init")
    @PreAuthorize("hasRole('ADMIN')")
    // ADMIN 권한으로 콘텐츠 업로드 초기화를 수행합니다.
    public ResponseEntity<SuccessResponse<ContentsUploadInitResponse>> createContentsUpload(
            @Valid @RequestBody ContentsUploadInitRequest request
    ) {
        return ResponseEntity.ok(SuccessResponse.of(contentsUploadService.createContentsUpload(request)));
    }
}
