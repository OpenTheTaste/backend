package com.ott.api_admin.tagging.service;

import com.ott.api_admin.ai.client.AiClient;
import com.ott.api_admin.tagging.event.AiTaggingRequestedEvent;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.common.Status;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.media_mood_tag.domain.MediaMoodTag;
import com.ott.domain.media_mood_tag.repository.MediaMoodTagRepository;
import com.ott.domain.mood_tag.domain.MoodTag;
import com.ott.domain.mood_tag.repository.MoodTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AITaggingAsyncService {

    private final AiClient aiClient;
    private final MediaRepository mediaRepository;
    private final MoodTagRepository moodTagRepository;
    private final MediaMoodTagRepository mediaMoodTagRepository;

    // 비동기 실행으로 관리자의 업로드 응답 속도에 영향을 주지 않도록 함
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional
    public void handleAiTagging(AiTaggingRequestedEvent event) {

        Long mediaId = event.mediaId();
        String description = event.description();

        log.info("[AI Tagging] 미디어 ID: {} 백그라운드로 태깅 분류 시작", mediaId);

        try {
            // ML에서 추론된 태그 리스트 -> 순서가 보장됨
            List<String> aiTags = aiClient.getEmotionTags(mediaId, description);

            if (aiTags == null || aiTags.isEmpty()) {
                log.info("[AI Tagging] 미디어 ID: {} - AI가 반환한 태그가 없습니다.", mediaId);
                return;
            }

            Media media = mediaRepository.findById(mediaId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEDIA_NOT_FOUND));

            // DB단에서 매핑된 리스트 -> 순서 보장 x
            List<MoodTag> foundMoodTags = moodTagRepository.findByNameInAndStatus(aiTags, Status.ACTIVE);

            // AI 태깅 리스트 <-> DB 리스트 일치
            Map<String, MoodTag> moodTagByName = foundMoodTags.stream()
                    .collect(Collectors.toMap(
                            MoodTag::getName,       // 키: 태그 이름
                            Function.identity(),    // 값: MoodTag 엔티티 자체
                            (left, right) -> left,  // 이름 중복 시 먼저 온 걸 사용
                            LinkedHashMap::new      // 순서 유지
                    ));

            // MediaMoodTag에 저장할 리스트
            List<MediaMoodTag> newMediaMoodTags = new ArrayList<>();

            // 중복 방지용
            Set<String> seen = new LinkedHashSet<>();

            for (int i = 0; i < aiTags.size(); i++) {
                String tagName = aiTags.get(i);
                MoodTag moodTag = moodTagByName.get(tagName);

                // DB에 없거나 중복태그일 경우 스킵
                if (moodTag == null || !seen.add(tagName)) {
                    continue;
                }

                newMediaMoodTags.add(MediaMoodTag.builder()
                        .media(media)
                        .moodTag(moodTag)
                        .priority(i + 1)    // 1부터 시작하는 우선순위
                        .build());
            }


            if (newMediaMoodTags.isEmpty()) {
                log.warn("[AI Tagging] 미디어 ID: {} - DB에 매핑 가능한 mood_tag가 없습니다. aiTags={}", mediaId, aiTags);
                return;
            }

            // DB <-> AI 태킹 불일치 확인용 로그
            List<String> missingTags = aiTags.stream()
                    .filter(tagName -> !moodTagByName.containsKey(tagName))
                    .distinct()
                    .toList();

            // 등록 후, 줄거리가 수정될 경우 변경할 경우 삭제 후 삽입
            mediaMoodTagRepository.deleteByMedia_Id(mediaId);
            mediaMoodTagRepository.saveAll(newMediaMoodTags);

            log.info("[AI Tagging] 미디어 ID: {} - mood tag {}건 저장 완료", mediaId, newMediaMoodTags.size());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[AI Tagging] 미디어 ID: {} - 태깅 저장 중 예외 발생", mediaId, e);
        }
    }
}
