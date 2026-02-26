package com.ott.api_user.comment.controller;

import com.ott.api_user.comment.dto.request.CreateCommentRequest;
import com.ott.api_user.comment.dto.response.CommentResponse;
import com.ott.api_user.comment.service.CommentService;
import com.ott.common.web.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    // 댓글 등록
    @PostMapping
    public ResponseEntity<SuccessResponse<CommentResponse>> creatComment(
           @Valid @RequestBody CreateCommentRequest request,
           @AuthenticationPrincipal Long memberId) {

        return ResponseEntity.ok(SuccessResponse.of(commentService.createComment(memberId, request)));
    }
}
