package com.ott.api_user.moodrefresh.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ott.api_user.moodrefresh.dto.response.MoodRefreshResponse;
import com.ott.api_user.moodrefresh.service.MoodRefreshService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mood-refresh")
public class MoodRefreshController {

    private final MoodRefreshService moodRefreshService;

    // 1. 유저가 홈 화면에 들어왔을 때 호출
    @GetMapping("/active")
    public ResponseEntity<MoodRefreshResponse> getActiveCard(@RequestAttribute("memberId") Long memberId) {
        return ResponseEntity.ok(moodRefreshService.getActiveRefreshCard(memberId));
    }

    // 2. 유저가 X(닫기) 버튼을 눌렀을 때 호출
    @PatchMapping("/{refreshId}/hide")
    public ResponseEntity<Void> hideCard(@PathVariable Long refreshId) {
        moodRefreshService.hideRefreshCard(refreshId);
        return ResponseEntity.noContent().build();
    }
}