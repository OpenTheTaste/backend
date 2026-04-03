package com.ott.api_user.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor
@Schema(description = "댓글 등록 요청 DTO")
public class CreateCommentRequest {

    @NotNull(message = "미디어 ID는 필수 입니다.")
    @Schema(type= "Long", example = "1", description = "미디어 ID")
    private Long mediaId;

    @NotBlank(message = "댓글 내용은 필수 입니다.")
    @Size(max = 100, message = "댓글은 100자 이내로 입력해주세요.")
    @Schema(type= "String", example = "아 ㅋㅋ 밤티하둥", description = "댓글")
    private String content;

    @NotNull(message = "스포 유무 입력은 필수 입니다.")
    @Schema(type= "Boolean", example = "true", description = "스포 유무, 디폴트 false")
    private Boolean isSpoiler = false;

}
