package com.ott.api_user.bookmark.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "북마크 등록/취소 요청 DTO")
public class BookmarkRequest {

    @NotNull(message = "mediaId는 필수입니다.")
    @Positive(message = "mediaId는 1 이상이어야 합니다.")
    @Schema(type ="Long", description = "북마크할 미디어 ID", example = "1")
    private Long mediaId;
}
