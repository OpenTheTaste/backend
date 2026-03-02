package com.ott.api_user.member.controller;

import com.ott.api_user.member.dto.request.SetPreferredTagRequest;
import com.ott.api_user.member.dto.request.UpdateMemberRequest;
import com.ott.api_user.member.dto.response.*;
import com.ott.api_user.member.service.MemberService;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // 태그 별 추천 리스트 조회
    @Override
    @GetMapping("/me/taglist/{tagId}")
    public ResponseEntity<SuccessResponse<List<TagContentResponse>>> getRecommendContentsByTag(
            @AuthenticationPrincipal Long memberId,
            @Positive @PathVariable Long tagId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(memberService.getRecommendContentsByTag(memberId, tagId)));
    }

    // 과거 시청 이력 조회, 10개씩 조회
    @Override
    @GetMapping("/me/history/playlist")
    public ResponseEntity<SuccessResponse<PageResponse<RecentWatchResponse>>> getWatchHistoryPlaylist(
            @AuthenticationPrincipal Long memberId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer page
    ) {
        return ResponseEntity.ok(SuccessResponse.of(memberService.getWatchHistoryPlaylist(memberId, page)));
    }

    // 회원 탈퇴 - 현재 soft delete
    // 현재 회원 탈퇴를 진행해도 JWT가 현재 stateless라서 만료 시간 까지 API 호출이 가능함
    // 추후 redis 블랙리스트 같은 기술을 도입해야됨
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal Long memberId) {
        memberService.withdraw(memberId);
        return ResponseEntity.noContent().build();
    }
}
