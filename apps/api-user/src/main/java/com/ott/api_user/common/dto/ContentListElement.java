package com.ott.api_user.common.dto;

import com.ott.domain.common.MediaType;
import com.ott.domain.media.domain.Media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "콘텐츠 리스트 공통 요소") // 홈화면 플레이리스트 요소들(인기차트 , 태그별 리스트, 최근 시청 리스트, 검색 결과 등등 )
public class ContentListElement {
    @Schema(description = "미디어 ID", example = "1")
    private Long id;

    @Schema(description = "미디어 타입", example = "SERIES")
    private MediaType mediaType;

    @Schema(description = "미디어 제목", example = "비밀의 숲")
    private String title;

    @Schema(description = "포스터 이미지 URL", example = "https://cdn.ott.com/posters/101.jpg")
    private String posterUrl;

    @Schema(description = "가로형 썸네일 이미지 URL", example = "https://cdn.ott.com/thumbnails/101.jpg")
    private String thumbnailUrl;

    public static ContentListElement from(Media media) {
        return ContentListElement.builder()
                .id(media.getId())
                .mediaType(media.getMediaType())
                .title(media.getTitle())
                .posterUrl(media.getPosterUrl())
                .thumbnailUrl(media.getThumbnailUrl())
                .build();
    }

}
