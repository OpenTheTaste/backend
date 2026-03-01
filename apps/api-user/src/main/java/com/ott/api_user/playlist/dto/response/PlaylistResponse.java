package com.ott.api_user.playlist.dto.response;

import com.ott.domain.common.MediaType;
import com.ott.domain.media.domain.Media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "플레이리스트 조회 응답 DTO")
public class PlaylistResponse {

    @Schema(description = "미디어 고유 ID", example = "100")
    private Long mediaId;

    @Schema(description = "컨텐츠 제목", example = "비밀의 숲")
    private String title;

    @Schema(description = "포스터 이미지 URL", example = "https://s3.../poster.jpg")
    private String posterUrl;

    @Schema(description = "미디어 타입 (UI 분기 처리 및 라우팅용)", example = "SERIES")
    private MediaType mediaType;

    public static PlaylistResponse from(Media media) {
        return PlaylistResponse.builder()
                .mediaId(media.getId())
                .title(media.getTitle())
                .posterUrl(media.getPosterUrl())
                .mediaType(media.getMediaType())
                .build();
    }
}
