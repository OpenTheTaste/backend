package com.ott.api_user.event;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.ott.api_user.moodrefresh.service.MoodRefreshService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MoodRefreshEventListenerTest {

    @Mock
    private MoodRefreshService moodRefreshService;

    @InjectMocks
    private MoodRefreshEventListener listener;

    @Test
    void handleWatchHistoryCreated_callsAnalyzeService() {
        Long memberId = 21L;

        listener.handleWatchHistoryCreated(new WatchHistoryCreatedEvent(memberId));

        verify(moodRefreshService).analyzeAndCreateRefreshCard(memberId);
    }

    @Test
    void handleWatchHistoryCreated_swallowsServiceExceptions() {
        Long memberId = 22L;
        doThrow(new RuntimeException("boom")).when(moodRefreshService).analyzeAndCreateRefreshCard(memberId);

        listener.handleWatchHistoryCreated(new WatchHistoryCreatedEvent(memberId));

        // 예외가 외부로 전파되지 않음을 검증하기 위해 별도 검증은 없음
    }
}
