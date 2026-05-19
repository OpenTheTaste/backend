package com.ott.api_user.playback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ott.api_user.playback.dto.request.PlaybackInitRequest;
import com.ott.api_user.playback.dto.request.PlaybackUpdateRequest;
import com.ott.api_user.playback.service.PlaybackService;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playback")
public class PlayBackController implements PlayBackApi {
    private final PlaybackService playbackService;

    @Override
    @PostMapping
    public ResponseEntity<Void> initPlayback(
        @AuthenticationPrincipal Long memberId,
        @Valid @RequestBody PlaybackInitRequest request) {

            playbackService.initPlayback(memberId, request.getMediaId());

            return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping
    public ResponseEntity<Void> updatePlayBack(
        @AuthenticationPrincipal Long memberId,
        @Valid @RequestBody PlaybackUpdateRequest request){

            playbackService.updatePlayback(memberId, request.getMediaId(), request.getPositionSec());

            return ResponseEntity.noContent().build();
    }
}
