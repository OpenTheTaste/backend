package com.ott.api_user.playback.service;

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

    public void initPlayback(Long memberId, Long mediaId) {
        Long contentsId = contentsRepository.findPlayableContentsIdByMediaId(mediaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENTS_NOT_FOUND));

        playbackRepository.insertIgnorePlayback(memberId, contentsId);
    }

    public void updatePlayback(Long memberId, Long mediaId, Integer positionSec) {

        if (positionSec == null || positionSec < 0) {
            positionSec = 0;
        }

        PlayableMediaCacheValue playableMedia = playbackValidationCacheService.getPlayableMedia(mediaId);
        if (!playableMedia.playable()) {
            throw new BusinessException(ErrorCode.CONTENTS_NOT_FOUND);
        }

        playbackRepository.updatePlayback(memberId, playableMedia.contentsId(), positionSec);
    }
}
