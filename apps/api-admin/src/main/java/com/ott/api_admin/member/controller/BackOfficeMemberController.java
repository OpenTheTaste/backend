package com.ott.api_admin.member.controller;

import com.ott.api_admin.member.dto.response.MemberListResponse;
import com.ott.api_admin.member.service.BackOfficeMemberService;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/back-office")
@RequiredArgsConstructor
public class BackOfficeMemberController implements BackOfficeMemberApi {

    private final BackOfficeMemberService backOfficeMemberService;

    @Override
    @GetMapping("/admin/members")
    public ResponseEntity<SuccessResponse<PageResponse<MemberListResponse>>> getMemberList(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(
                SuccessResponse.of(backOfficeMemberService.getMemberList(page, size))
        );
    }
}
