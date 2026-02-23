package com.ott.api_admin.upload.controller;

import com.ott.api_admin.upload.dto.request.SeriesUploadInitRequest;
import com.ott.api_admin.upload.dto.response.SeriesUploadInitResponse;
import com.ott.api_admin.upload.service.SeriesUploadService;
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
 * 시리즈 업로드 초기화 요청을 처리하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/back-office/admin/upload/series")
@RequiredArgsConstructor
public class SeriesUploadController implements SeriesUploadApi {

    private final SeriesUploadService seriesUploadService;

    @Override
    @PostMapping("/init")
    @PreAuthorize("hasRole('ADMIN')")
    // ADMIN 권한으로 시리즈 업로드 초기화를 수행합니다.
    public ResponseEntity<SuccessResponse<SeriesUploadInitResponse>> createSeriesUpload(
            @Valid @RequestBody SeriesUploadInitRequest request
    ) {
        return ResponseEntity.ok(SuccessResponse.of(seriesUploadService.createSeriesUpload(request)));
    }
}
