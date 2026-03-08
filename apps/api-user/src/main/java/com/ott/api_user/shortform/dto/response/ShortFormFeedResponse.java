package com.ott.api_user.shortform.dto.response;

import java.time.LocalDateTime;

import com.ott.domain.common.MediaType;
import com.ott.domain.media.domain.Media;
import com.ott.domain.short_form.domain.ShortForm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "숏폼 피드 응답 DTO")
public class ShortFormFeedResponse {
    @Schema(type= "Long", description = "숏폼 ID", example = "1")
    private Long shortFormId;

    @Schema(type = "String", description = "숏폼 제목", example = "더글로리 명장면 1분 요약")
    private String title;

    @Schema(type = "String", description = "에디터(업로더) 이름", example = "오픈더테이스트_공식")
    private String editorName;

    @Schema(type = "LocalDateTime", description = "업로드 날짜")
    private LocalDateTime uploadDate;

    @Schema(type = "Boolean", description = "북마크 여부", example = "false")
    private Boolean isBookmarked;

    @Schema(type = "Boolean", description = "좋아요 여부", example = "true")
    private Boolean isLiked;

    @Schema(type = "String", description = "숏폼 영상 URL", example = "https://openthetaste.cloud/video/short_1.mp4")
    private String shortMasterPlaylistUrl;

    @Schema(type = "Long", description = "연결된 본편 미디어 ID", example = "105")
    private Long originMediaId;

    @Schema(type = "String", description = "연결된 본편의 미디어 타입 (UI 분기 처리 및 라우팅용)", example = "SERIES")
    private MediaType mediaType;


   public static ShortFormFeedResponse of(ShortForm shortForm, Boolean isBookmarked, Boolean isLiked) {
        // 엔티티에 이미 구현된 findOriginMedia() 활용
        Media originMedia = shortForm.findOriginMedia().orElse(null);

        return ShortFormFeedResponse.builder()
                .shortFormId(shortForm.getId())
                .title(shortForm.getMedia().getTitle())
                .editorName(shortForm.getMedia().getUploader().getNickname())
                .uploadDate(shortForm.getCreatedDate())
                .isBookmarked(isBookmarked)
                .isLiked(isLiked)
                .shortMasterPlaylistUrl(shortForm.getMasterPlaylistUrl())
                .originMediaId(originMedia != null ? originMedia.getId() : null)
                .mediaType(originMedia != null ? originMedia.getMediaType() : null)
                .build();
    }

}
