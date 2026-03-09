package com.ott.api_user.series.dto;

import com.ott.domain.contents.domain.Contents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "시리즈 내 콘텐츠(에피소드) 목록 아이템 응답 DTO")
public class SeriesContentsResponse {
    @Schema(type = "Long", example = "1", description = "에피소드의 미디어 ID")
    private Long mediaId;

    @Schema(type = "Long" , example = "101", description = "시리즈 본체의 미디어 ID")
    private Long seriesMediaId;


    @Schema(type = "String", example = "더 글로리 시즌 1: 1화", description = "콘텐츠 제목")
    private String title;

    @Schema(type = "String", example = "추락하는 자에겐 날개가 없다...", description = "콘텐츠 설명")
    private String description;

    @Schema(type = "String", example = "https://cdn.ott.com/thumbnails/c101.jpg", description = "콘텐츠 썸네일")
    private String thumbnailUrl;

    @Schema(type = "Integer", example = "3600", description = "재생 시간 (초)")
    private Integer duration;

    @Schema(type = "Integer", example = "1200", description = "사용자가 마지막으로 시청한 지점 (초)")
    private Integer positionSec;

    public static SeriesContentsResponse from(Contents content, Integer positionSec) {
        return SeriesContentsResponse.builder()
                .mediaId(content.getMedia().getId())
                .seriesMediaId(content.getSeries().getMedia().getId())
                .duration(content.getDuration())
                .title(content.getMedia().getTitle())
                .description(content.getMedia().getDescription())
                .thumbnailUrl(content.getMedia().getThumbnailUrl())
                .positionSec(positionSec != null ? positionSec : 0)
                .build();
    }
}
