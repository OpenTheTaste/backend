package com.ott.api_user.comment.dto;

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
public class CommentResponse {
    @Schema(description = "댓글 고유 ID", example = "1")
    private Long commentId;

    @Schema(description = "작성자 닉네임", example = "영화광문어")
    private String nickname;

    @Schema(description = "댓글 내용", example = "이 영화 진짜 시간 가는 줄 모르고 봤네요!! 강추합니다.")
    private String content;

    @Schema(description = "스포일러 여부", example = "true")
    private Boolean isSpoiler;

    @Schema(description = "작성 일시")
    private LocalDateTime createdAt;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .nickname(comment.getMember().getNickname())
                .content(comment.getContent())
                .isSpoiler(comment.getIsSpoiler())
                .createdAt(comment.getCreatedDate())
                .build();
    }
}
