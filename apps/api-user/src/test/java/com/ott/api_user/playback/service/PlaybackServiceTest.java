package com.ott.api_user.playback.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.playback.repository.PlaybackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaybackServiceTest {

    @Mock
    private PlaybackRepository playbackRepository;

    @Mock
    private ContentsRepository contentsRepository;

    @InjectMocks
    private PlaybackService playbackService;

    @Test
    void upsertPlayback_defaultsNegativePositionToZero() {
        Long memberId = 10L;
        Long mediaId = 20L;
        when(playbackRepository.upsertPlaybackByMediaId(memberId, mediaId, 0)).thenReturn(1);

        playbackService.upsertPlayback(memberId, mediaId, -5);

        verify(playbackRepository).upsertPlaybackByMediaId(memberId, mediaId, 0);
        verify(contentsRepository, never()).existsPlayableByMediaId(anyLong());
    }

    @Test
    void upsertPlayback_defaultsNullPositionToZero() {
        Long memberId = 11L;
        Long mediaId = 21L;
        when(playbackRepository.upsertPlaybackByMediaId(memberId, mediaId, 0)).thenReturn(1);

        playbackService.upsertPlayback(memberId, mediaId, null);

        verify(playbackRepository).upsertPlaybackByMediaId(memberId, mediaId, 0);
        verify(contentsRepository, never()).existsPlayableByMediaId(anyLong());
    }

    @Test
    void upsertPlayback_succeedsWhenAffectedRowsIsZeroButPlayableContentExists() {
        Long memberId = 1L;
        Long mediaId = 5L;
        when(playbackRepository.upsertPlaybackByMediaId(memberId, mediaId, 10)).thenReturn(0);
        when(contentsRepository.existsPlayableByMediaId(mediaId)).thenReturn(true);

        playbackService.upsertPlayback(memberId, mediaId, 10);

        verify(playbackRepository).upsertPlaybackByMediaId(memberId, mediaId, 10);
        verify(contentsRepository).existsPlayableByMediaId(mediaId);
    }

    @Test
    void upsertPlayback_throwsWhenPlayableContentMissingAfterZeroAffectedRows() {
        when(playbackRepository.upsertPlaybackByMediaId(1L, 5L, 10)).thenReturn(0);
        when(contentsRepository.existsPlayableByMediaId(5L)).thenReturn(false);

        assertThatThrownBy(() -> playbackService.upsertPlayback(1L, 5L, 10))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONTENTS_NOT_FOUND);
    }

    @Test
    void upsertPlayback_skipsFallbackWhenAffectedRowsIsPositive() {
        Long memberId = 2L;
        Long mediaId = 6L;
        when(playbackRepository.upsertPlaybackByMediaId(memberId, mediaId, 15)).thenReturn(2);

        playbackService.upsertPlayback(memberId, mediaId, 15);

        verify(playbackRepository).upsertPlaybackByMediaId(memberId, mediaId, 15);
        verify(contentsRepository, never()).existsPlayableByMediaId(anyLong());
    }
}
