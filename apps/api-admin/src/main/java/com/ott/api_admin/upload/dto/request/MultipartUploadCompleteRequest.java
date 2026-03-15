package com.ott.api_admin.upload.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "멀티파트 업로드 완료 요청")
public record MultipartUploadCompleteRequest(
        @Schema(type = "String", description = "S3 object key", example = "contents/10/origin/origin.mp4")
        @NotBlank
        String objectKey,

        @Schema(type = "String", description = "S3 멀티파트 upload ID")
        @NotBlank
        String uploadId,

        @Schema(description = "업로드된 파트 eTag 목록")
        @NotEmpty
        List<@Valid @NotNull PartETagRequest> parts
) {
    public record PartETagRequest(
            @Schema(type = "Integer", description = "파트 번호", example = "1")
            @Positive
            int partNumber,

            @Schema(type = "String", description = "파트 eTag")
            @NotBlank
            String eTag
    ) {
    }
}
