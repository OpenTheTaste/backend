package com.ott.api_admin.tagging.service;

import com.ott.api_admin.ai.client.AiClient;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AITaggingAsyncService {

    private final AiClient aiClient;
    private final MediaRepository mediaRepository;
    private final MoodTagRepository moodTagRepository;
    private final MediaMoodTagRepository mediaMoodTagRepository;

    // 비동기 실행으로 관리자의 업로드 응답 속도에 영향을 주지 않도록 함
    @Transactional
    @Async
    public void processAiTagging(Long mediaId, String description) {
        log.info("[AI Tagging] 미디어 ID: {} 백그라운드로 태깅 분류 시작", mediaId);

        try {
            List<String> aiTags = aiClient.getEmotionTags(mediaId, description);

            if (aiTags == null || aiTags.isEmpty()) {
                log.info("[AI Tagging] 미디어 ID: {} - AI가 반환한 태그가 없습니다.", mediaId);
                return;
            }

            Media media = mediaRepository.findById(mediaId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEDIA_NOT_FOUND));

            List<MoodTag> foundMoodTags = moodTagRepository.findByNameInAndStatus(aiTags, Status.ACTIVE);
            Map<String, MoodTag> moodTagByName = foundMoodTags.stream()
                    .collect(Collectors.toMap(MoodTag::getName, Function.identity(), (left, right) -> left, LinkedHashMap::new));

            List<MediaMoodTag> newMediaMoodTags = IntStream.range(0, aiTags.size())
                    .mapToObj(index -> Map.entry(index, aiTags.get(index)))
                    .filter(entry -> moodTagByName.containsKey(entry.getValue()))
                    .filter(entry -> aiTags.indexOf(entry.getValue()) == entry.getKey())
                    .map(entry -> MediaMoodTag.builder()
                            .media(media)
                            .moodTag(moodTagByName.get(entry.getValue()))
                            .priority(entry.getKey())
                            .build())
                    .toList();

            if (newMediaMoodTags.isEmpty()) {
                log.warn("[AI Tagging] 미디어 ID: {} - DB에 매핑 가능한 mood_tag가 없습니다. aiTags={}", mediaId, aiTags);
                return;
            }

            List<String> missingTags = aiTags.stream()
                    .filter(tagName -> !moodTagByName.containsKey(tagName))
                    .distinct()
                    .toList();

            if (!missingTags.isEmpty()) {
                log.warn("[AI Tagging] 미디어 ID: {} - DB에 없는 mood_tag를 제외합니다. missingTags={}", mediaId, missingTags);
            }

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
