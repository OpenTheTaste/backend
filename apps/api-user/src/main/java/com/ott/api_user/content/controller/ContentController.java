package com.ott.api_user.content.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ott.api_user.content.dto.ContentDetailResponse;
import com.ott.api_user.content.service.ContentService;
import com.ott.common.web.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contents")
public class ContentController implements ContentApi {
    private final ContentService contentService;

    @Override
    public ResponseEntity<SuccessResponse<ContentDetailResponse>> getContentDetail(
            @PathVariable(value = "contentsId") Long contentsId,
            @AuthenticationPrincipal Long memberId) {

        return ResponseEntity.ok(
                SuccessResponse.of(contentService.getContentDetail(contentsId, memberId)));
    }

    // 플레이 리스트 API

    // 댓글 조회 API

}
