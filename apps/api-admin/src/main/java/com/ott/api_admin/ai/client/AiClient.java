package com.ott.api_admin.ai.client;

import com.ott.api_admin.ai.dto.TaggingRequest;
import com.ott.api_admin.ai.dto.TaggingResponse;

import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
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

        try{
            TaggingResponse response = aiWebClient.post()
                .uri("/tagging")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(TaggingResponse.class)
                .timeout(Duration.ofSeconds(5))
                .block(); // 비동기 작업 내에서 안전하게 블로킹 처리

        log.info("[Admin AI] 태깅 응답 완료: {}", response.getMoodTags());
        return response.getMoodTags(); 
        }catch(Exception e){
            // 타임아웃 발생 or AI 서버 죽었을 때
            log.error("[Admin AI] 태깅 요청 실패 (mediaId={}): {}", mediaId, e.getMessage());

            return Collections.emptyList(); // 에러로 중단되지 않게 일단 빈 값으로 반환
        }
    }
}