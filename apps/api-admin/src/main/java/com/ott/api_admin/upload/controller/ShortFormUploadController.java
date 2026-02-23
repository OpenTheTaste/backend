package com.ott.api_admin.upload.controller;

import com.ott.api_admin.upload.dto.request.ShortFormUploadInitRequest;
import com.ott.api_admin.upload.dto.response.ShortFormUploadInitResponse;
import com.ott.api_admin.upload.service.ShortFormUploadService;
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
 * 숏폼 업로드 초기화 요청을 처리하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/back-office/admin/upload/short-forms")
@RequiredArgsConstructor
public class ShortFormUploadController implements ShortFormUploadApi {

    private final ShortFormUploadService shortFormUploadService;

    @Override
    @PostMapping("/init")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    // ADMIN 또는 EDITOR 권한으로 숏폼 업로드 초기화를 수행합니다.
    public ResponseEntity<SuccessResponse<ShortFormUploadInitResponse>> createShortFormUpload(
            @Valid @RequestBody ShortFormUploadInitRequest request
    ) {
        return ResponseEntity.ok(SuccessResponse.of(shortFormUploadService.createShortFormUpload(request)));
    }
}
