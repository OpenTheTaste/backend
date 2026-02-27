package com.ott.api_user.comment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.ott.api_user.comment.dto.CommentResponse;
import com.ott.api_user.content.dto.ContentDetailResponse;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Comment", description = "댓글 API")
public interface CommentApi {
        @Operation(summary = "콘텐츠 댓글 목록 조회", description = "콘텐츠의 댓글 목록을 페이징하여 최신순으로 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)) }),
                        @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)) })
        })
        @GetMapping("/contents/{contentsId}/comments")
        ResponseEntity<SuccessResponse<PageResponse<CommentResponse>>> getContentCommentsList(
                        @Parameter(description = "콘텐츠 ID", required = true, example = "10") @PathVariable("contentsId") Long contentsId,
                        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(value = "page", defaultValue = "0") Integer page,
                        @Parameter(description = "페이지 크기", example = "20") @RequestParam(value = "size", defaultValue = "20") Integer size,
                        @Parameter(description = "스포일러 포함 여부 (true: 전체 조회, false: 스포 제외)", example = "false") @RequestParam(value = "includeSpoiler", defaultValue = "false") boolean includeSpoiler);
}