package com.ott.api_user.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "댓글 수정 요청 DTO")
public class UpdateCommentRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 100, message = "댓글은 100자 이내로 입력해주세요")
    @Schema(type= "String", example = "아 ㅋㅋ 밤티하둥_수정123", description = "댓글")
    private String content;

    @Schema(type= "Boolean", example = "true", description = "스포일러 포함 여부, 디폴트 false")
    private Boolean isSpoiler;
}
