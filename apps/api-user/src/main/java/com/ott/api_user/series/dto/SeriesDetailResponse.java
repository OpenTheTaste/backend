package com.ott.api_user.series.dto;

import java.util.List;

import com.ott.domain.series.domain.Series;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "시리즈 상세 조회 응답 DTO")
public class SeriesDetailResponse {

    @Schema(type = "Long", description = "미디어 고유 ID", example = "101")
    private Long mediaId;

    @Schema(type ="String", description = "시리즈 제목", example = "비밀의 숲")
    private String title;

    @Schema(type ="String", description = "시리즈 설명", example = "검경 수사극의 새로운 지평을 연 드라마")
    private String description;

    @Schema(type ="String", description = "출연진", example = "송혜교, 이도현, 임지연")
    private String actors;

    @Schema(type ="String", description = "가로형 썸네일 이미지 URL", example = "https://cdn.ott.com/thumbnails/101.jpg")
    private String thumbnailUrl;

    @Schema(type ="String", description = "카테고리", example = "드라마")
    private String category;

    @Schema(type = "Array", description = "태그 목록", example = "드라마, 범죄, 수사")
    private List<String> tagList;

    @Schema(type = "Boolean", description = "사용자 북마크 여부", example = "true")
    private Boolean isBookmarked;

    @Schema(type = "Boolean", description = "사용자 좋아요 여부", example = "true")
    private Boolean isLiked;

    @Schema(type = "Long", description = "이어볼 에피소드의 Media ID. 시청 이력이 없으면 1화 ID 반환", example = "501")
    private Long resumeMediaId;

    // 정적 팩토리 메서드 사용
    public static SeriesDetailResponse of(Series series, List<String> tags, List<String> categories,
            Boolean isBookmarked, Boolean isLiked , Long resumeMediaId) {
        return SeriesDetailResponse.builder()
                .mediaId(series.getMedia().getId())
                .actors(series.getActors())
                .title(series.getMedia().getTitle())
                .description(series.getMedia().getDescription())
                .thumbnailUrl(series.getMedia().getThumbnailUrl())
                .category(categories.isEmpty() ? null : categories.get(0))
                .tagList(tags)
                .isBookmarked(isBookmarked)
                .isLiked(isLiked)
                .resumeMediaId(resumeMediaId)
                .build();
    }

}
