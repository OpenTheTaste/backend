package com.ott.api_user.likes.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "좋아요 요청 DTO")
public class LikesRequest {

    @NotNull(message = "mediaId는 필수입니다.")
    @Schema(type = "Long", description = "좋아요 할 미디어 ID", example = "1")
    private Long mediaId;
}
