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
@Schema(description = "내 댓글 목록 응답 DTO")
public class MyCommentResponse {

    @Schema(type = "Long", example = "1", description = "댓글 ID")
    private Long commentId;

    @Schema(type = "String", example = "주인공 43:23초에 죽음 ㅋㅋ", description = "댓글 내용")
    private String content;

    @Schema(type = "String", example = "https://cdn.example.com/poster.jpg", description = "콘텐츠 포스터 URL")
    private String contentsPosterUrl;

    @Schema(type = "Long", example = "10", description = "작성자 회원 ID")
    private Long writerId;

    @Schema(type = "String", example = "김마루", description = "작성자 닉네임")
    private String writerNickname;

    @Schema(type = "string", format = "date-time", example = "2024-01-01T00:00:00", description = "작성일시")
    private LocalDateTime createdDate;

    public static MyCommentResponse from(Comment comment) {
        return MyCommentResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .contentsPosterUrl(comment.getContents().getMedia().getPosterUrl())
                .writerId(comment.getMember().getId())
                .writerNickname(comment.getMember().getNickname())
                .createdDate(comment.getCreatedDate())
                .build();
    }
}