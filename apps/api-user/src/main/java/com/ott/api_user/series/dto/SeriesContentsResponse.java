package com.ott.api_user.series.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "시리즈 내 콘텐츠(에피소드) 목록 아이템 응답 DTO")
public class SeriesContentsResponse {
    @Schema(type = "Long", example = "1", description = "콘텐츠 고유 ID")
    private Long id;

    @Schema(type = "String", example = "더 글로리 시즌 1: 1화", description = "콘텐츠 제목")
    private String title;

    @Schema(type = "String", example = "추락하는 자에겐 날개가 없다...", description = "콘텐츠 설명")
    private String description;

    @Schema(type = "String", example = "https://cdn.ott.com/thumbnails/c101.jpg", description = "콘텐츠 썸네일")
    private String thumbnailUrl;

    @Schema(type = "Integer", example = "3600", description = "재생 시간 (초)")
    private Integer duration;

    // 이어보기 지점도 응답에 포함
    @Schema(type = "Integer", example = "1200", description = "사용자가 마지막으로 시청한 지점 (초)")
    private Integer positionSec;
}
