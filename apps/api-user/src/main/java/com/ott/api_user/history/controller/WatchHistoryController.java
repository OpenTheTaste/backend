package com.ott.api_user.history.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ott.api_user.history.dto.request.WatchHistoryRequest;
import com.ott.api_user.history.service.WatchHistoryService;
import com.ott.common.web.response.SuccessResponse;
import com.ott.domain.watch_history.repository.WatchHistoryRepository;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/watch-history")
public class WatchHistoryController implements WatchHistoryApi{
    
    private final WatchHistoryService watchHistoryService;

    @Override
    @PutMapping
    public ResponseEntity<SuccessResponse<Void>> createWatchHistory(
        @AuthenticationPrincipal Long memberId,
        @Valid @RequestBody WatchHistoryRequest request){
            
            watchHistoryService.updateWatchHistory(memberId, request.getMediaId());
                return ResponseEntity.ok(SuccessResponse.of(null));
        }
}

