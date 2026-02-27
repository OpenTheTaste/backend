package com.ott.api_user.comment.controller;

import com.ott.api_user.comment.dto.request.CreateCommentRequest;
import com.ott.api_user.comment.dto.request.UpdateCommentRequest;
import com.ott.api_user.comment.dto.response.CommentResponse;
import com.ott.api_user.comment.service.CommentService;
import com.ott.common.web.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController implements CommentApi {

    private final CommentService commentService;

    // 댓글 등록
    @Override
    @PostMapping
    public ResponseEntity<SuccessResponse<CommentResponse>> createComment(
           CreateCommentRequest request, Long memberId) {

        return ResponseEntity.ok(SuccessResponse.of(commentService.createComment(memberId, request)));
    }

    // 댓글 수정
    @Override
    @PutMapping("/{commentId}")
    public ResponseEntity<SuccessResponse<CommentResponse>> updateComment(
            Long commentId, UpdateCommentRequest request, Long memberId) {

        return ResponseEntity.ok(SuccessResponse.of(commentService.updateComment(memberId, commentId, request)));
    }
}
