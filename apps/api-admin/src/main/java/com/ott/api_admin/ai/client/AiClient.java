package com.ott.api_admin.ai.client;

import com.ott.api_admin.ai.dto.TaggingRequest;
import com.ott.api_admin.ai.dto.TaggingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

    private final WebClient aiWebClient;

    /**
     * FastAPI 서버에 영상 줄거리를 보내고 감정 태그 리스트를 받아옵니다.
     */
    public List<String> getEmotionTags(Long mediaId, String description) {
        log.info("[Admin AI] 미디어 태깅 요청: mediaId={}", mediaId);

        TaggingRequest requestDto = new TaggingRequest(mediaId, description);

        TaggingResponse response = aiWebClient.post()
                .uri("/tagging")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(TaggingResponse.class)
                .block(); // 비동기 작업 내에서 안전하게 블로킹 처리

        log.info("[Admin AI] 태깅 응답 완료: {}", response.getMoodTags());
        return response.getMoodTags(); 
    }
}