package com.ott.api_user.bookmark.controller;

import com.ott.api_user.bookmark.dto.request.BookmarkRequest;
import com.ott.api_user.bookmark.dto.response.BookmarkMediaResponse;
import com.ott.api_user.bookmark.dto.response.BookmarkShortFormResponse;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Bookmark API", description = "북마크 관련 API입니다.")
public interface BookmarkAPI {
    @Operation(summary = "북마크 편집", description = "미디어에 대한 북마크를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "북마크 수정 성공"),
            @ApiResponse(responseCode = "404", description = "미디어 또는 사용자를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping
    ResponseEntity<SuccessResponse<Void>> editBookmark(
            @Valid @RequestBody BookmarkRequest request,
            @Parameter(hidden = true)  @AuthenticationPrincipal Long memberId
    );


    // 북마크 콘텐츠 or 시리즈 목록 조회
    @Operation(summary = "북마크 콘텐츠 목록 조회", description = "유저가 북마크한 콘텐츠 목록을 페이징으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "북마크 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class)))
    })
    @GetMapping("/me/contents")
    ResponseEntity<SuccessResponse<PageResponse<BookmarkMediaResponse>>> getBookmarkMediaList(
            @Parameter(description = "페이지 번호 (0부터 시작)", required = true) @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기", required = true) @RequestParam(defaultValue = "10") @Min(0) @Max(100) int size,
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    );


    // 북마크 숏폼 목록 조회
    @Operation(summary = "북마크 숏폼 목록 조회", description = "유저가 북마크한 숏폼 목록을 페이징으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "북마크 숏폼 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class)))
    })
    @GetMapping("/me/short-form")
    ResponseEntity<SuccessResponse<PageResponse<BookmarkShortFormResponse>>> getBookmarkShortFormList(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") @Min(0) @Max(100) int size,
            @Parameter(hidden = true) Long memberId
    );


}

