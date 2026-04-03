package com.ott.api_user.comment.controller;

import com.ott.api_user.comment.dto.request.CreateCommentRequest;
import com.ott.api_user.comment.dto.request.UpdateCommentRequest;
import com.ott.api_user.comment.dto.response.CommentResponse;
import com.ott.api_user.comment.dto.response.ContentsCommentResponse;
import com.ott.api_user.comment.dto.response.MyCommentResponse;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.media.ArraySchema;


@Tag(name = "Comment API", description = "댓글 API")
@SecurityRequirement(name = "BearerAuth")
@RequestMapping("/comments")
public interface CommentApi {

        @Operation(summary = "댓글 작성", description = "콘텐츠에 댓글을 작성합니다. 스포일러 기본값은 false입니다.")
        @ApiResponses({
                @ApiResponse(responseCode = "201", description = "댓글 작성 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "401", description = "인증 실패",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "콘텐츠 또는 회원을 찾을 수 없음",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PostMapping
        ResponseEntity<SuccessResponse<CommentResponse>> createComment(
                @Valid @RequestBody CreateCommentRequest request,
                @AuthenticationPrincipal @Parameter(hidden = true) Long memberId
        );

        @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글을 수정합니다.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "401", description = "인증 실패",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "403", description = "본인 댓글이 아님",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PatchMapping("/{commentId}")
        ResponseEntity<SuccessResponse<CommentResponse>> updateComment(
                @Positive @Parameter(description = "댓글 ID") @PathVariable Long commentId,
                @Valid @RequestBody UpdateCommentRequest request,
                @AuthenticationPrincipal @Parameter(hidden = true) Long memberId
        );

        @Operation(summary = "댓글 삭제", description = "본인이 작성한 댓글을 삭제합니다.")
        @ApiResponses({
                @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "403", description = "본인 댓글이 아님",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class)))
        })
        @DeleteMapping("/{commentId}")
        ResponseEntity<Void> deleteComment(
                @Positive @Parameter(description = "댓글 ID") @PathVariable Long commentId,
                @AuthenticationPrincipal @Parameter(hidden = true) Long memberId
        );

        @Operation(summary = "내가 작성한 댓글 목록 조회", description = "내가 작성한 댓글 목록을 최신순으로 조회합니다.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "조회 성공",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = MyCommentResponse.class))),

                @ApiResponse(responseCode = "401", description = "인증 실패",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/me")
        ResponseEntity<SuccessResponse<PageResponse<MyCommentResponse>>> getMyComments(
                @PositiveOrZero @Parameter(description = "페이지 번호 (0부터 시작)", schema = @Schema(type = "Integer", defaultValue = "0")) @RequestParam(defaultValue = "0") Integer page,
                @Positive @Parameter(description = "페이지 크기", schema = @Schema(type = "Integer", defaultValue = "20")) @RequestParam(defaultValue = "20") Integer size,
                @AuthenticationPrincipal @Parameter(hidden = true) Long memberId
        );


        @Operation(summary = "콘텐츠 댓글 목록 조회", description = "콘텐츠의 댓글 목록을 페이징하여 최신순으로 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "0", description = "조회 성공 - 콘텐츠 댓글 목록 구성", content = {
                                        @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ContentsCommentResponse.class))) }),
                        @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)) }),
                        @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)) })
        })
        @GetMapping("/{mediaId}/comments")
        ResponseEntity<SuccessResponse<PageResponse<ContentsCommentResponse>>> getContentCommentsList(
                        @Parameter(description = "미디어 ID", required = true, example = "10") @PathVariable("mediaId") Long contentsId,
                        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(value = "page", defaultValue = "0") Integer page,
                        @Parameter(description = "페이지 크기", example = "20") @RequestParam(value = "size", defaultValue = "20") Integer size,
                        @Parameter(description = "스포일러 포함 여부 (true: 전체 조회, false: 스포 제외)", example = "false") @RequestParam(value = "includeSpoiler", defaultValue = "false") boolean includeSpoiler,
                        @AuthenticationPrincipal @Parameter(hidden = true) Long memberId);

}
