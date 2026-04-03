package com.ott.api_admin.upload.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "멀티파트 파트 업로드 presigned URL 응답")
public record MultipartUploadPartUrlResponse(
        @Schema(type = "Integer", description = "파트 번호", example = "1")
        int partNumber,

        @Schema(type = "String", description = "해당 파트 업로드용 presigned URL")
        String uploadUrl
) {
}
