package com.ott.api_user.bookmark.dto.response;

import com.ott.domain.bookmark.domain.Bookmark;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "북마크 숏폼 목록 응답 DTO")
public class BookmarkShortFormResponse {

    @Schema(type ="Long", description = "미디어 ID", example = "1")
    private Long mediaId;

    @Schema(type ="String", description = "미디어 제목", example = "김마루의 숏폼 EP 01")
    private String title;

    @Schema(type ="String", description = "미디어 설명", example = "오늘은 북마크 API를 작성했다. 참 재미있었다!")
    private String description;

    @Schema(type ="String", description = "썸네일 URL", example = "https://cdn.ott.com/thumbnails/1.jpg")
    private String thumbnailUrl;

    public static BookmarkShortFormResponse from(Bookmark bookmark) {
        return BookmarkShortFormResponse.builder()
                .mediaId(bookmark.getMedia().getId())
                .title(bookmark.getMedia().getTitle())
                .description(bookmark.getMedia().getDescription())
                .thumbnailUrl(bookmark.getMedia().getThumbnailUrl())
                .build();
    }
}