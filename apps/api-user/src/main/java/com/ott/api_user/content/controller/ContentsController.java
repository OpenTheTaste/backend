package com.ott.api_user.content.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ott.api_user.common.ContentSource;
import com.ott.api_user.common.dto.ContentListElement;
import com.ott.api_user.content.dto.ContentsDetailResponse;
import com.ott.api_user.content.service.ContentsService;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/contents")
public class ContentsController implements ContentsApi {

        private final ContentsService contentService;

        @Override
        public ResponseEntity<SuccessResponse<ContentsDetailResponse>> getContentDetail(
                        @PathVariable(value = "contentsId") Long contentsId,
                        @AuthenticationPrincipal Long memberId) {

                return ResponseEntity.ok(
                                SuccessResponse.of(contentService.getContentDetail(contentsId, memberId)));
        }

        // 재생목록(플레이리스트) 리스트 API (/contents/{contentsId}/playlist?source={SOURCE})
        @Override
        public ResponseEntity<SuccessResponse<PageResponse<ContentListElement>>> getContentPlayList(
                        @PathVariable(value = "contentsId") Long contentsId,
                        @RequestParam(value = "source", required = false) ContentSource source,
                        @RequestParam(value = "page") @Min(0) Integer pageParam,
                        @RequestParam(value = "size") @Positive Integer sizeParam,
                        @AuthenticationPrincipal Long memberId) {
                return ResponseEntity.ok(
                                SuccessResponse.of(contentService.getContentPlayList(contentsId, source, pageParam,
                                                sizeParam, memberId)));
        }
}
