package com.ott.api_user.comment.dto.response;

import java.time.LocalDateTime;

import com.ott.domain.comment.domain.Comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "댓글 조회 응답 DTO")
public class ContentsCommentResponse {
    @Schema(type = "Long", description = "댓글 고유 ID", example = "1")
    private Long commentId;

    @Schema(type = "String", description = "작성자 닉네임", example = "영화광문어")
    private String nickname;

    @Schema(type = "String", description = "댓글 내용", example = "이 영화 진짜 시간 가는 줄 모르고 봤네요!! 강추합니다.")
    private String content;

    @Schema(type = "Boolean", description = "스포일러 여부", example = "true")
    private boolean isSpoiler;

    @Schema(type = "LocalDateTime", description = "작성 일시")
    private LocalDateTime createdAt;

    @Schema(type = "Boolean", description = "본인 댓글 여부(수정/삭제 버튼 노출용)", example = "true")
    private boolean isMine;

    public static ContentsCommentResponse from(Comment comment, boolean isMine) {
        return ContentsCommentResponse.builder()
                .commentId(comment.getId())
                .nickname(comment.getMember().getNickname())
                .content(comment.getContent())
                .isSpoiler(comment.getIsSpoiler())
                .createdAt(comment.getCreatedDate())
                .isMine(isMine)
                .build();
    }
}
