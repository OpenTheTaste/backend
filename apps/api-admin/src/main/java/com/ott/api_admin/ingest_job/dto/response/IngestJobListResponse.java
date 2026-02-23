package com.ott.api_admin.ingest_job.dto.response;

import com.ott.domain.ingest_job.domain.IngestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "업로드 작업 목록 조회 응답")
public record IngestJobListResponse(

        @Schema(type = "Long", description = "작업 ID", example = "1")
        Long ingestJobId,

        @Schema(type = "String", description = "미디어(시리즈/콘텐츠/숏폼) 제목", example = "비밀의 숲 1화")
        String title,

        @Schema(type = "Integer", description = "영상 크기(KB)", example = "1048576")
        Integer videoSize,

        @Schema(type = "String", description = "업로더 닉네임", example = "홍길동")
        String uploaderName,

        @Schema(type = "String", description = "작업 상태", example = "TRANSCODING")
        IngestStatus ingestStatus,

        @Schema(type = "Integer", description = "진행률 (%)", example = "0")
        Integer progress
) {
}
