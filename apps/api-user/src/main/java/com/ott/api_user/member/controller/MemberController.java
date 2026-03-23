package com.ott.api_user.member.controller;

import com.ott.api_user.member.dto.request.SetPreferredTagRequest;
import com.ott.api_user.member.dto.request.UpdateMemberRequest;
import com.ott.api_user.member.dto.response.*;
import com.ott.api_user.member.service.MemberService;
import com.ott.common.security.util.CookieUtil;
import com.ott.common.web.response.SuccessResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController implements MemberApi {

    @Value("${app.auth.cookie.access-name:userAccessToken}")
    private String accessCookieName;

    @Value("${app.auth.cookie.refresh-name:userRefreshToken}")
    private String refreshCookieName;

    private final MemberService memberService;
    private final CookieUtil cookie;

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
        return ResponseEntity.noContent().build();
    }


    // 회원 탈퇴 - 현재 soft delete
    // 회원 탈퇴 시 DB + 브라우저 토큰 삭제
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(
            HttpServletResponse response,
            @AuthenticationPrincipal Long memberId) {
        memberService.withdraw(memberId);

        cookie.deleteCookie(response, accessCookieName);
        cookie.deleteCookie(response, refreshCookieName);

        return ResponseEntity.noContent().build();
    }

    // 온보딩 스킵 시 온보딩 컬럼 true 변경
    @Override
    @PostMapping("/me/onboarding/skip")
    public ResponseEntity<Void> skipOnboarding(@AuthenticationPrincipal Long memberId) {
        memberService.skipOnboarding(memberId);
        return ResponseEntity.noContent().build();
    }
}
