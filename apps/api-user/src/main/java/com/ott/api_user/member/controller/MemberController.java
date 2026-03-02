package com.ott.api_user.member.controller;

import com.ott.api_user.member.dto.request.SetPreferredTagRequest;
import com.ott.api_user.member.dto.request.UpdateMemberRequest;
import com.ott.api_user.member.dto.response.MyPageResponse;
import com.ott.api_user.member.dto.response.TagMonthlyCompareResponse;
import com.ott.api_user.member.dto.response.TagRankingResponse;
import com.ott.api_user.member.service.MemberService;
import com.ott.common.web.response.SuccessResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController implements MemberApi {

    private final MemberService memberService;

    @Override
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<MyPageResponse>> getMyPage(@AuthenticationPrincipal Long memberId) {
        MyPageResponse response = memberService.getMyPage(memberId);
        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    @Override
    @PatchMapping("/me")
    public ResponseEntity<SuccessResponse<MyPageResponse>> updateMyInfo(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody UpdateMemberRequest request
    ) {
        return ResponseEntity.ok(SuccessResponse.of(memberService.updateMyInfo(memberId, request)));

    }

    @Override
    @PostMapping("/me/tags")
    public ResponseEntity<SuccessResponse<Void>> setPreferredTags(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody SetPreferredTagRequest request
    ) {
        memberService.setPreferredTags(memberId, request);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    // 유저 별 1달 간 상위 태그 조회
    @Override
    @GetMapping("/me/tag/ranking")
    public ResponseEntity<SuccessResponse<TagRankingResponse>> getTagRanking(
            @AuthenticationPrincipal Long memberId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(memberService.getTagRanking(memberId)));
    }

    // 유저 별 2달 간 특정 태그 조회
    @Override
    @GetMapping("/me/tag/ranking/{tagId}")
    public ResponseEntity<SuccessResponse<TagMonthlyCompareResponse>> getTagMonthlyCompare(
            @AuthenticationPrincipal Long memberId,
            @Positive @PathVariable Long tagId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(memberService.getTagMonthlyCompare(memberId, tagId)));
    }
}
