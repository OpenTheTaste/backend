package com.ott.api_user.playlist.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ott.api_user.common.dto.ContentListElement;
import com.ott.api_user.playlist.dto.request.PlaylistCondition;
import com.ott.api_user.playlist.dto.response.PlaylistResponse;
import com.ott.api_user.playlist.service.PlaylistService;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playlist") 
public class PlaylistController implements PlaylistApi {
    
    private final PlaylistService playlistService;

    @Override
    public ResponseEntity<SuccessResponse<PageResponse<PlaylistResponse>>> getPlaylists(
            PlaylistCondition condition,
            @RequestParam(value = "page", defaultValue = "0") Integer pageParam,
            @RequestParam(value = "size", defaultValue = "10") Integer sizeParam,
            @AuthenticationPrincipal Long memberId) {
            
        // 토큰에서 추출한 유저 ID를 Condition 객체에 세팅
        if (memberId != null) {
            condition.setMemberId(memberId);
        }

        Pageable pageable = PageRequest.of(pageParam, sizeParam);

        return ResponseEntity.ok(SuccessResponse.of(playlistService.getPlaylists(condition, pageable)));
    }
}