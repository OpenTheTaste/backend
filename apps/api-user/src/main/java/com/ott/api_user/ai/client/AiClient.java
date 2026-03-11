package com.ott.api_user.ai.client;

import com.ott.api_user.ai.dto.MoodRefreshRequest;
import com.ott.api_user.ai.dto.MoodRefreshResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

    private final WebClient aiWebClient;

    /**
     * FastAPI 서버에 최근 부정적 태그 시퀀스를 보내고 환기용 타겟 태그를 받아옵니다.
     */
    public List<String> getTargetTags(List<String> inputTags) {
        log.info("[User AI] 타겟 태그 예측 요청: inputTags={}", inputTags);

        MoodRefreshRequest requestDto = new MoodRefreshRequest(inputTags);

        try{
             MoodRefreshResponse response = aiWebClient.post()
                .uri("/recommend/mood-refresh/target")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(MoodRefreshResponse.class)
                .timeout(Duration.ofSeconds(5))
                .block(); // 비동기 작업 내에서 안전하게 블로킹 처리

            log.info("[User AI] 타겟 태그 응답 완료: {}", response.getOutputTags());
            return response.getOutputTags();
        }catch(Exception e){
            log.error("[User AI] 타겟 태그 요청 실패 (inputTags={}): {}", inputTags, e.getMessage());
            // 관리자 로직이 에러로 멈추지 않도록 빈 리스트를 반환합니다. (안전장치)
            return Collections.emptyList();
        }
    }
}