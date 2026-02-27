package com.ott.api_user.comment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ott.api_user.comment.dto.CommentResponse;
import com.ott.api_user.comment.service.CommentService;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CommentController implements CommentApi {

    private final CommentService commentService;

    @Override
    public ResponseEntity<SuccessResponse<PageResponse<CommentResponse>>> getContentCommentsList(
            @PathVariable(value = "contentsId") Long contentsId,
            @RequestParam(value = "page", defaultValue = "0") Integer pageParam,
            @RequestParam(value = "size", defaultValue = "20") Integer sizeParam,
            @RequestParam(value = "includeSpoiler", defaultValue = "false") boolean includeSpoiler) {
        return ResponseEntity.ok(
                SuccessResponse.of(commentService.getContentsCommentList(contentsId, pageParam,
                        sizeParam, includeSpoiler)));

    }
}
