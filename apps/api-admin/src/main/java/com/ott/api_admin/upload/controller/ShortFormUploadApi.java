package com.ott.api_admin.upload.controller;

import com.ott.api_admin.upload.dto.request.ShortFormUploadInitRequest;
import com.ott.api_admin.upload.dto.response.ShortFormUploadInitResponse;
import com.ott.common.web.response.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 숏폼 업로드 초기화 API 명세입니다.
 */
public interface ShortFormUploadApi {
    /**
     * 숏폼 업로드에 필요한 DB 레코드와 Presigned URL을 생성합니다.
     */
    ResponseEntity<SuccessResponse<ShortFormUploadInitResponse>> createShortFormUpload(
            @RequestBody ShortFormUploadInitRequest request
    );
}
