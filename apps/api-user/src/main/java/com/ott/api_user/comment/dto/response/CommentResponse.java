package com.ott.api_user.comment.dto.response;

import com.ott.domain.comment.domain.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "댓글 응답 DTO")
public class CommentResponse {

    @Schema(type = "Long", example = "1", description = "댓글 ID")
    private Long commentId;

    @Schema(type = "Long", example = "5", description = "콘텐츠 ID")
    private Long contentsId;

    @Schema(type = "String", example = "밤티 하네 ㅋㅋ", description = "댓글 내용")
    private String content;

    @Schema(type = "Boolean", example = "false", description = "스포일러 포함 여부")
    private Boolean isSpoiler;

    @Schema(type = "LocalDateTime ", example = "2026.02.15 20:14", description = "작성일시")
    private LocalDateTime createdDate;

    @Schema(description = "작성자 정보")
    private WriterInfo writer;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .contentsId(comment.getContents().getId())
                .content(comment.getContent())
                .isSpoiler(comment.getIsSpoiler())
                .createdDate(comment.getCreatedDate())
                .writer(WriterInfo.from(comment))
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "댓글 작성자 정보")
    public static class WriterInfo {

        @Schema(type = "Long", example = "10", description = "작성자 회원 ID")
        private Long memberId;

        @Schema(type = "String", example = "김마루", description = "작성자 닉네임")
        private String nickname;

        public static WriterInfo from(Comment comment) {
            return WriterInfo.builder()
                    .memberId(comment.getMember().getId())
                    .nickname(comment.getMember().getNickname())
                    .build();
        }
    }
}