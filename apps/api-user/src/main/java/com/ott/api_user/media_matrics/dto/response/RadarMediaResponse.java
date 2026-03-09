package com.ott.api_user.media_matrics.dto.response;

import com.ott.domain.common.MediaType;
import com.ott.domain.media.domain.Media;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "레이더 차트 추천 미디어 응답")
public class RadarMediaResponse {

    @Schema(description = "미디어 고유 ID", example = "100")
    private Long mediaId;

    @Schema(description = "제목", example = "비밀의 숲")
    private String title;

    @Schema(description = "포스터 이미지 URL", example = "https://s3.../poster.jpg")
    private String posterUrl;

    @Schema(description = "썸네일 이미지 URL", example = "https://cdn.ott.com/thumbnails/101.jpg")
    private String thumbnailUrl;

    @Schema(description = "미디어 타입", example = "SERIES")
    private MediaType mediaType;

    public static RadarMediaResponse from(Media media) {
        return RadarMediaResponse.builder()
                .mediaId(media.getId())
                .title(media.getTitle())
                .posterUrl(media.getPosterUrl())
                .thumbnailUrl(media.getThumbnailUrl())
                .mediaType(media.getMediaType())
                .build();
    }
}
