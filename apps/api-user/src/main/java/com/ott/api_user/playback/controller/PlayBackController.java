package com.ott.api_user.playback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ott.api_user.playback.dto.request.PlaybackUpdateRequest;
import com.ott.api_user.playback.service.PlaybackService;
import com.ott.common.web.response.SuccessResponse;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playback")
public class PlayBackController implements PlayBackApi {
    private final PlaybackService playbackService;

    @Override
    @PutMapping
    public ResponseEntity<SuccessResponse<Void>> createPlayBack(
        @AuthenticationPrincipal Long memberId,
        @Valid @RequestBody PlaybackUpdateRequest request){

            playbackService.updatePlayback(memberId, request.getMediaId(), request.getPositionSec());

            return ResponseEntity.ok(SuccessResponse.of(null));
    }
}
