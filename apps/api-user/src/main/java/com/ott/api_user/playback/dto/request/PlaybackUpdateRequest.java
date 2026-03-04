package com.ott.api_user.playback.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "이어보기 요청 DTO")
public class PlaybackUpdateRequest {

    @Schema(type = "Long", description = "미디어 ID", example = "101")
    private Long mediaId;

    @Schema(type = "Integer", description = "재생 지점(초)", example = "120")
    private Integer positionSec;
}
