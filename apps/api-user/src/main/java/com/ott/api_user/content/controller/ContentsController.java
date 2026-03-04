package com.ott.api_user.content.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ott.api_user.content.dto.ContentsDetailResponse;
import com.ott.api_user.content.service.ContentsService;
import com.ott.common.web.response.SuccessResponse;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/contents")
public class ContentsController implements ContentsApi {

        private final ContentsService contentService;

        @Override
        public ResponseEntity<SuccessResponse<ContentsDetailResponse>> getContentDetail(
                        @PathVariable(value = "mediaId") Long mediaId,
                        @AuthenticationPrincipal Long memberId) {

                return ResponseEntity.ok(
                                SuccessResponse.of(contentService.getContentDetail(mediaId, memberId)));
        }
}
