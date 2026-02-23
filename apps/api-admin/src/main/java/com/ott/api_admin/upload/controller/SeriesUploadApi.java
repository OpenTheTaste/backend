package com.ott.api_admin.upload.controller;

import com.ott.api_admin.upload.dto.request.SeriesUploadInitRequest;
import com.ott.api_admin.upload.dto.response.SeriesUploadInitResponse;
import com.ott.common.web.response.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 시리즈 업로드 초기화 API 명세입니다.
 */
public interface SeriesUploadApi {
    /**
     * 시리즈 업로드에 필요한 DB 레코드와 Presigned URL을 생성합니다.
     */
    ResponseEntity<SuccessResponse<SeriesUploadInitResponse>> createSeriesUpload(
            @RequestBody SeriesUploadInitRequest request
    );
}
