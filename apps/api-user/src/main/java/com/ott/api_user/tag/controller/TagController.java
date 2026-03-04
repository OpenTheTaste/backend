package com.ott.api_user.tag.controller;

import com.ott.api_user.tag.dto.response.TagMonthlyCompareResponse;
import com.ott.api_user.tag.dto.response.TagRankingResponse;
import com.ott.api_user.tag.service.TagService;
import com.ott.common.web.response.SuccessResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController implements TagAPI {

    private final TagService tagService;


    // 유저 별 1달 간 상위 태그 조회
    @Override
    @GetMapping("/me/ranking")
    public ResponseEntity<SuccessResponse<TagRankingResponse>> getTagRanking(
            @AuthenticationPrincipal Long memberId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(tagService.getTagRanking(memberId)));
    }

    // 유저 별 2달 간 특정 태그 조회
    @Override
    @GetMapping("/me/ranking/{tagId}")
    public ResponseEntity<SuccessResponse<TagMonthlyCompareResponse>> getTagMonthlyCompare(
            @AuthenticationPrincipal Long memberId,
            @Positive @PathVariable Long tagId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(tagService.getTagMonthlyCompare(memberId, tagId)));
    }
}
