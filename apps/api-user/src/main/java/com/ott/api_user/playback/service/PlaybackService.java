package com.ott.api_user.playback.service;

import com.ott.api_user.playback.buffer.PlaybackCommandQueue;
import com.ott.api_user.playback.dto.request.PlaybackInitRequest;
import com.ott.api_user.playback.dto.request.PlaybackUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ott.api_user.playback.cache.PlayableMediaCacheValue;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.playback.repository.PlaybackRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaybackService {

    private final PlaybackRepository playbackRepository;
    private final PlaybackValidationCacheService playbackValidationCacheService;
    private final ContentsRepository contentsRepository;
    private final PlaybackCommandQueue playbackCommandQueue;

    public void initPlayback(Long memberId, PlaybackInitRequest playbackInitRequest) {
        Long contentsId = contentsRepository.findPlayableContentsIdByMediaId(playbackInitRequest.getMediaId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENTS_NOT_FOUND));

        playbackRepository.insertIgnorePlayback(memberId, contentsId);
    }

    public void updatePlayback(Long memberId, PlaybackUpdateRequest playbackUpdateRequest) {
        PlayableMediaCacheValue playableMedia = playbackValidationCacheService.getPlayableMedia(playbackUpdateRequest.getMediaId());
        if (!playableMedia.playable()) {
            throw new BusinessException(ErrorCode.CONTENTS_NOT_FOUND);
        }

        boolean offered = playbackCommandQueue.offer(
            memberId, playableMedia.contentsId(), playbackUpdateRequest.getPositionSec());

        if (!offered) {
            playbackCommandQueue.offerToOverflow(
                memberId, playableMedia.contentsId(), playbackUpdateRequest.getPositionSec());
        }
    }
}
