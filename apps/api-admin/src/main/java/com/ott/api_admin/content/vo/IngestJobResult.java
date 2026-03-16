package com.ott.api_admin.content.vo;

import com.ott.domain.common.MediaType;

public record IngestJobResult(
        Long mediaId,
        Long ingestJobId,
        String originObjectKey,
        Long fileSize,
        MediaType mediaType
) {
}
