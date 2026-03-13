package com.ott.api_user.moodrefresh.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ott.api_user.moodrefresh.dto.response.MoodRefreshResponse;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.moodrefresh.domain.MemberMoodRefresh;
import com.ott.domain.moodrefresh.repository.MemberMoodRefreshRepository;

import lombok.RequiredArgsConstructor;

// 환기 카드를 보여주고 사용자가 닫기 버튼을 누르면 숨겨주는 로직 작성
@Service
@RequiredArgsConstructor
public class MoodRefreshService {
    private final MemberMoodRefreshRepository refreshRepository;
    private final MediaRepository mediaRepository; // 영상 상세 정보 조회를 위해 필요

    // 홈 화면에서 활성화된 카드 보여주기
    @Transactional(readOnly = true)
    public MoodRefreshResponse getActiveRefreshCard(Long memberId) {
        MemberMoodRefresh activeCard = refreshRepository.findTopByMemberIdAndIsHiddenFalseOrderByCreatedAtDesc(memberId)
                .orElseThrow(() -> new RuntimeException("현재 활성화된 환기 카드가 없습니다."));

        // JSON으로 저장된 리스트(1, 2, 3)를 꺼내서, 실제 DB 영상(Media) 데이터로 찾아옴
        List<Long> mediaIds = activeCard.getRecommendedMediaIds();
        List<Media> mediaList = mediaRepository.findAllById(mediaIds);

        return MoodRefreshResponse.of(activeCard, mediaList);
    }

    // 닫기 버튼 누르면 카드 숨기기
    @Transactional
    public void hideRefreshCard(Long refreshId) {
        MemberMoodRefresh card = refreshRepository.findById(refreshId)
                .orElseThrow(() -> new RuntimeException("해당 카드를 찾을 수 없습니다."));
        
        card.hideCard(); // isHidden 을 true로 바꿈
    }
}
