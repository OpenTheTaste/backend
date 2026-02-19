package com.ott.api_user.series.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "시리즈 상세 조회 응답 DTO")
public class SeriesDetailResponse {

    @Schema(description = "시리즈 고유 ID", example = "101")
    private Long id;

    @Schema(description = "시리즈 제목", example = "비밀의 숲")
    private String title;

    @Schema(description = "시리즈 설명", example = "검경 수사극의 새로운 지평을 연 드라마")
    private String description;

    @Schema(description = "출연진", example = "송혜교, 이도현, 임지연")
    private String actors;

    @Schema(description = "시리즈 포스터 이미지 URL", example = "https://cdn.ott.com/posters/101.jpg")
    private String posterUrl;

    @Schema(description = "썸네일 이미지 URL", example = "https://cdn.ott.com/thumbnails/101.jpg")
    private String thumbnailUrl;

    @Schema(description = "카테고리", example = "드라마")
    private String category;

    @Schema(description = "태그 목록", example = "드라마, 범죄, 수사")
    private List<String> tags;

    @Schema(description = "사용자 북마크 여부", example = "true")
    private Boolean isBookmarked;

    @Schema(description = "사용자 좋아요 여부", example = "true")
    private Boolean isLiked;

}
