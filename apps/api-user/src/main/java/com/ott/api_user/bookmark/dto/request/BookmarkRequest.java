package com.ott.api_user.bookmark.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "북마크 등록/취소 요청 DTO")
public class BookmarkRequest {

    @NotNull(message = "mediaId는 필수입니다.")
    @Schema(description = "북마크할 미디어 ID", example = "1")
    private Long mediaId;
}
