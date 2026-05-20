package com.ott.api_user.playback.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ott.api_user.playback.cache.PlayableMediaCacheValue;
import com.ott.api_user.playback.dto.request.PlaybackInitRequest;
import com.ott.api_user.playback.dto.request.PlaybackUpdateRequest;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.playback.repository.PlaybackRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PlaybackServiceTest {

    @Mock
    private PlaybackRepository playbackRepository;

    @Mock
    private PlaybackValidationCacheService playbackValidationCacheService;

    @Mock
    private ContentsRepository contentsRepository;

    @InjectMocks
    private PlaybackService playbackService;

    @Test
    void initPlayback_insertsIgnorePlaybackWithZeroPosition() {
        Long memberId = 1L;
        Long mediaId = 10L;
        Long contentsId = 100L;
        when(contentsRepository.findPlayableContentsIdByMediaId(mediaId)).thenReturn(Optional.of(contentsId));

        playbackService.initPlayback(memberId, createInitRequest(mediaId));

        verify(playbackRepository).insertIgnorePlayback(memberId, contentsId);
    }

    @Test
    void initPlayback_throwsWhenPlayableContentMissing() {
        when(contentsRepository.findPlayableContentsIdByMediaId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playbackService.initPlayback(1L, createInitRequest(10L)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONTENTS_NOT_FOUND);
    }

    @Test
    void updatePlayback_passesNegativePositionAsIs() {
        Long memberId = 10L;
        Long mediaId = 20L;
        when(playbackValidationCacheService.getPlayableMedia(mediaId))
                .thenReturn(PlayableMediaCacheValue.valid(30L));

        playbackService.updatePlayback(memberId, createUpdateRequest(mediaId, -5));

        verify(playbackRepository).updatePlayback(memberId, 30L, -5);
    }

    @Test
    void updatePlayback_usesContentsIdResolvedByValidationCache() {
        Long memberId = 1L;
        Long mediaId = 5L;
        when(playbackValidationCacheService.getPlayableMedia(mediaId))
                .thenReturn(PlayableMediaCacheValue.valid(100L));

        playbackService.updatePlayback(memberId, createUpdateRequest(mediaId, 10));

        verify(playbackRepository).updatePlayback(memberId, 100L, 10);
    }

    @Test
    void updatePlayback_throwsWhenPlayableContentMissing() {
        when(playbackValidationCacheService.getPlayableMedia(5L))
                .thenReturn(PlayableMediaCacheValue.invalid());

        assertThatThrownBy(() -> playbackService.updatePlayback(1L, createUpdateRequest(5L, 10)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONTENTS_NOT_FOUND);
    }

    @Test
    void updatePlayback_updatesPlaybackWhenPlayableContentExists() {
        Long memberId = 2L;
        Long mediaId = 6L;
        when(playbackValidationCacheService.getPlayableMedia(mediaId))
                .thenReturn(PlayableMediaCacheValue.valid(200L));

        playbackService.updatePlayback(memberId, createUpdateRequest(mediaId, 15));

        verify(playbackRepository).updatePlayback(memberId, 200L, 15);
    }

    private PlaybackInitRequest createInitRequest(Long mediaId) {
        PlaybackInitRequest request = new PlaybackInitRequest();
        ReflectionTestUtils.setField(request, "mediaId", mediaId);
        return request;
    }

    private PlaybackUpdateRequest createUpdateRequest(Long mediaId, Integer positionSec) {
        PlaybackUpdateRequest request = new PlaybackUpdateRequest();
        ReflectionTestUtils.setField(request, "mediaId", mediaId);
        ReflectionTestUtils.setField(request, "positionSec", positionSec);
        return request;
    }
}
