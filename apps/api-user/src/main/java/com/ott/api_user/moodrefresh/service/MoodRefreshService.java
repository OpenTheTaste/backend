package com.ott.api_user.moodrefresh.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ott.api_user.ai.client.AiClient;
import com.ott.api_user.moodrefresh.dto.response.MoodRefreshResponse;
import com.ott.domain.common.Status;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media_mood_tag.domain.MediaMoodTag;
import com.ott.domain.media_mood_tag.repository.MediaMoodTagRepository;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.moodrefresh.domain.MemberMoodRefresh;
import com.ott.domain.moodrefresh.repository.MemberMoodRefreshRepository;
import com.ott.domain.watch_history.domain.WatchHistory;
import com.ott.domain.watch_history.repository.WatchHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MoodRefreshService {
    private final MemberMoodRefreshRepository refreshRepository;
    private final MediaRepository mediaRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final AiClient aiClient;
    private final MediaMoodTagRepository mediaMoodTagRepository;

    // 홈 화면에 노출시킬 카드 
    @Transactional(readOnly = true)
    public MoodRefreshResponse getActiveRefreshCard(Long memberId) {
        MemberMoodRefresh activeCard = refreshRepository
                .findTopByMemberIdAndIsHiddenFalseOrderByCreatedAtDesc(memberId)
                .orElseThrow(() -> new RuntimeException("현재 활성화된 환기 카드가 없습니다."));

        List<Long> mediaIds = activeCard.getRecommendedMediaIds();
        List<Media> mediaList = mediaRepository.findAllById(mediaIds);

        return MoodRefreshResponse.of(activeCard, mediaList);
    }

    // 카드 숨김 요청 시
    @Transactional
    public void hideRefreshCard(Long refreshId) {
        MemberMoodRefresh card = refreshRepository.findById(refreshId)
                .orElseThrow(() -> new RuntimeException("해당 카드를 찾을 수 없습니다."));
        card.hideCard();
    }


    // 이벤트리스너 시작 시 - 메인 로직 
    @Transactional
    public void analyzeAndCreateRefreshCard(Long memberId) {

        // 해당 멤버의 쿨다운 확인
        if (isInCooldown(memberId)) {
            log.info("[Mood Refresh] 쿨다운(6시간) 통과 실패 - memberId: {}", memberId);
            return;
        }

        LocalDateTime seventyTwoHoursAgo = LocalDateTime.now().minusHours(72);
        
        // 최근 72시간 내 가장 최근 3개 시청 기록 가져오기
        List<WatchHistory> recentHistories = watchHistoryRepository
                .findRecentUnusedHistoriesWithin(memberId, seventyTwoHoursAgo, 3);

        if (recentHistories.size() < 3) {
            log.debug("[Mood Refresh] 최근 72시간 이력 부족 - memberId: {}, records={}", memberId, recentHistories.size());
            return;
        }

        if (!isAllSameMoodCategory(recentHistories)) {
            log.debug("[Mood Refresh] 대표 감정 그룹 불일치 - memberId: {}", memberId);
            return;
        }

        List<String> inputTags = extractTagsFromHistories(recentHistories);
        log.info("[Mood Refresh] Gate 통과 - memberId: {}, inputTags={}", memberId, inputTags);

        // TODO: 파이썬 AI, 추천 미디어, 카드 저장 처리 이어서 구현
    }



    // 위 메인 로직에 필요한 필터링 메서드

    // 1차 필터링: 쿨타임 확인용
    private boolean isInCooldown(Long memberId) {
        LocalDateTime sixHoursAgo = LocalDateTime.now().minusHours(6);
        return refreshRepository.existsByMemberIdAndCreatedDateAfter(memberId, sixHoursAgo);
    }

    // 2차 필터링: 최근 시청 이력 (72시간 내)의 감정 태그의 대표 카테고리가 같은 감정 카테고리인지 확인
    private boolean isAllSameMoodCategory(List<WatchHistory> histories) {
        List<Long> mediaIds = histories.stream()
                .map(history -> history.getContents().getMedia().getId())
                .distinct()
                .toList();

        // 1순위(priority = 0) 태그들 한 번에 IN 절로 조회
        List<MediaMoodTag> primaryTags = mediaMoodTagRepository
                .findByMedia_IdInAndStatusAndPriorityOrderByMedia_IdAscPriorityAsc(mediaIds, Status.ACTIVE, 0);

        // 태그가 누락된 영상이 하나라도 있다면 환기 불가로 간주
        if (primaryTags.size() < mediaIds.size()) {
            return false;
        }

        // Stream을 이용해 대분류 카테고리 ID만 추출하고 중복을 제거하여 1개인지 확인
        long distinctCategoryCount = primaryTags.stream()
                .map(tag -> tag.getMoodTag().getMoodCategory().getId()) // 카테고리 ID 추출
                .distinct() // 중복 제거
                .count();   // 남은 개수 카운트

        return distinctCategoryCount == 1; // 1개면 모두 같은 감정 그룹!
    }

    // 3차 필터링: 3개의 시청 이력에서 각 영상당 상위 3개의 감정 태그를 추출하여 중복 없이 병합
    private List<String> extractTagsFromHistories(List<WatchHistory> histories) {
        List<Long> mediaIds = histories.stream()
                .map(history -> history.getContents().getMedia().getId())
                .distinct()
                .toList();

        // 3개 영상에 달린 모든 활성 태그 조회
        List<MediaMoodTag> allMoodTags = mediaMoodTagRepository
                .findByMedia_IdInAndStatusOrderByMedia_IdAscPriorityAsc(mediaIds, Status.ACTIVE);

        return allMoodTags.stream()
                // 1. 미디어 ID별로 그룹화 하면서 태그 이름만 리스트로 묶음
                .collect(Collectors.groupingBy(
                        tag -> tag.getMedia().getId(),
                        Collectors.mapping(tag -> tag.getMoodTag().getName(), Collectors.toList())
                ))
                .values().stream()
                // 2. 각 영상당 상위 3개의 태그만 자름 (limit 3)
                .flatMap(tags -> tags.stream().limit(3)) 
                .filter(StringUtils::hasText) // 3. 빈 문자열 방어
                .distinct() // 4. 최종적으로 남은 태그들의 중복 제거
                .toList();
    }

    
}
