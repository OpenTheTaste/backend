package com.ott.api_user.playlist.controller;

import com.ott.api_user.playlist.dto.response.RecentWatchResponse;
import com.ott.api_user.playlist.service.PlaylistService;
import com.ott.api_user.playlist.dto.response.TagPlaylistResponse;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/playlists")
@RequiredArgsConstructor
public class PlaylistController implements PlayListAPI {

    private final PlaylistService playlistService;

    // 태그 별 추천 리스트 조회
    @Override
    @GetMapping("/me/{tagId}")
    public ResponseEntity<SuccessResponse<List<TagPlaylistResponse>>> getRecommendContentsByTag(
            @AuthenticationPrincipal Long memberId,
            @Positive @PathVariable Long tagId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(playlistService.getRecommendContentsByTag(memberId, tagId)));
    }

    // 과거 시청 이력 조회, 10개씩 조회
    @Override
    @GetMapping("/me/history")
    public ResponseEntity<SuccessResponse<PageResponse<RecentWatchResponse>>> getWatchHistoryPlaylist(
            @AuthenticationPrincipal Long memberId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer page
    ) {
        return ResponseEntity.ok(SuccessResponse.of(playlistService.getWatchHistoryPlaylist(memberId, page)));
    }
}
