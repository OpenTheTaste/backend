package com.ott.api_user.bookmark.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ott.api_user.bookmark.dto.request.BookmarkRequest;
import com.ott.api_user.bookmark.service.BookmarkService;
import com.ott.common.web.response.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookmarks")
public class BookmarkController implements BookmarkAPI  {

    private final BookmarkService bookmarkService;

    // 북마크 수정
    @Override
    public ResponseEntity<SuccessResponse<Void>> editBookmark(
            @Valid @RequestBody BookmarkRequest request,
            @AuthenticationPrincipal Long memberId) {

        bookmarkService.editBookmark(memberId, request.getMediaId());
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

}