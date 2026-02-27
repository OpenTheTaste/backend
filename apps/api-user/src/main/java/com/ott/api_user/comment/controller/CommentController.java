package com.ott.api_user.comment.controller;

import com.ott.api_user.comment.dto.request.CreateCommentRequest;
import com.ott.api_user.comment.dto.request.UpdateCommentRequest;
import com.ott.api_user.comment.dto.response.CommentResponse;
import com.ott.api_user.comment.dto.response.MyCommentResponse;
import com.ott.api_user.comment.service.CommentService;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
           @RequestBody CreateCommentRequest request,
           @AuthenticationPrincipal Long memberId) {

        return ResponseEntity.ok(SuccessResponse.of(commentService.createComment(memberId, request)));
    }

    // 댓글 수정
    @Override
    @PutMapping("/{commentId}")
    public ResponseEntity<SuccessResponse<CommentResponse>> updateComment(
            @PathVariable Long commentId,
            @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal Long memberId) {

        return ResponseEntity.ok(SuccessResponse.of(commentService.updateComment(memberId, commentId, request)));
    }

    // 댓글 삭제
    @Override
    @DeleteMapping("/{commentId}")
    public ResponseEntity<SuccessResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long memberId) {

        commentService.deleteComment(memberId, commentId);

        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    // 댓글 조회 - 본인 댓글만 조회 가능, 최신순 정렬
    @Override
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<PageResponse<MyCommentResponse>>> getMyComments(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(SuccessResponse.of(commentService.getMyComments(memberId, page, size)));
    }

}
