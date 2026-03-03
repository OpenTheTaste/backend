package com.ott.api_user.bookmark.dto.response;

import com.ott.domain.bookmark.domain.Bookmark;
import com.ott.domain.common.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "북마크한 콘텐츠 목록 응답 DTO")
public class BookmarkMediaResponse {

    @Schema(type ="Long", description = "미디어 ID", example = "1")
    private Long mediaId;

    @Schema(type = "String", description = "미디어 타입 (CONTENTS, SERIES)", example = "SERIES")
    private MediaType mediaType;

    @Schema(type ="String", description = "미디어 제목", example = "어서와요 김마루의 숲")
    private String title;

    @Schema(type ="String", description = "미디어 설명", example = "허거덩의 숲에서 힐링을 즐겨봐요~")
    private String description;

    @Schema(type ="String", description = "포스터 URL", example = "https://cdn.ott.com/posters/1.jpg")
    private String posterUrl;

    public static BookmarkMediaResponse from(Bookmark bookmark) {
        return BookmarkMediaResponse.builder()
                .mediaId(bookmark.getMedia().getId())
                .mediaType(bookmark.getMedia().getMediaType())
                .title(bookmark.getMedia().getTitle())
                .description(bookmark.getMedia().getDescription())
                .posterUrl(bookmark.getMedia().getPosterUrl())
                .build();
    }
}
