package com.ott.api_admin.shortform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "숏폼 수정 응답")
public record ShortFormUpdateResponse(
        @Schema(type = "Long", description = "숏폼 ID", example = "10")
        Long shortFormId,

        @Schema(type = "String", description = "포스터 Object Key(교체하지 않으면 null)", example = "short-forms/10/poster/poster-new.jpg")
        String posterObjectKey,

        @Schema(type = "String", description = "썸네일 Object Key(교체하지 않으면 null)", example = "short-forms/10/thumbnail/thumb-new.jpg")
        String thumbnailObjectKey,

        @Schema(type = "String", description = "원본 영상 Object Key(교체하지 않으면 null)", example = "short-forms/10/origin/origin-new.mp4")
        String originObjectKey,

        @Schema(type = "String", description = "마스터 플레이리스트 Object Key", example = "short-forms/10/transcoded/master.m3u8")
        String masterPlaylistObjectKey,

        @Schema(type = "String", description = "포스터 업로드 URL(교체하지 않으면 null)", example = "~/short-forms/10/poster/poster-new.jpg?X-Amz-...")
        String posterUploadUrl,

        @Schema(type = "String", description = "썸네일 업로드 URL(교체하지 않으면 null)", example = "~/short-forms/10/thumbnail/thumb-new.jpg?X-Amz-...")
        String thumbnailUploadUrl,

        @Schema(type = "String", description = "원본 영상 업로드 URL(교체하지 않으면 null)", example = "~/short-forms/10/origin/origin-new.mp4?X-Amz-...")
        String originUploadUrl
) {
}
