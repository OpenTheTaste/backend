package com.ott.api_user.member.controller;

import com.ott.api_user.member.dto.response.MyPageResponse;
import com.ott.api_user.member.service.MemberService;
import com.ott.common.web.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController implements MemberApi {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<MyPageResponse>> getMyPage(@AuthenticationPrincipal Long memberId) {
        MyPageResponse response = memberService.getMyPage(memberId);
        return ResponseEntity.ok(SuccessResponse.of(response));
    }
}
