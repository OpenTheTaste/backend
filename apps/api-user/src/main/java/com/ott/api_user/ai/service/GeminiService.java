package com.ott.api_user.ai.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ott.api_user.ai.dto.GeminiRequest;
import com.ott.api_user.ai.dto.GeminiResponse;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


@Service
public class GeminiService {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;

    public GeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateHealingMessage(String userMood, List<String> recommendedTags) {
        // 1. 파이썬에서 받은 태그 리스트를 하나의 문자열로 결합 ("힐링, 도파민_폭발, 평화로운")
        String tagsStr = String.join(", ", recommendedTags);
        
        // 2. 💡 프롬프트 엔지니어링 (핵심!)
        String promptText = String.format(
            "너는 영상 스트리밍 서비스 O+T(Open the Taste)의 다정하고 센스 있는 AI 큐레이터야. " +
            "지금 유저가 '%s' 기분을 느끼고 있어서, 분위기 환기를 위해 '%s' 느낌의 영상들을 추천해주려고 해. " +
            "유저의 현재 마음에 깊이 공감해주면서, 우리가 추천하는 이 영상들이 어떻게 작은 위로가 되거나 기분 전환이 될 수 있는지 " +
            "친근하고 따뜻한 말투로 2~3문장 이내로 말해줘. (예: '오늘 많이 지치셨군요. O+T가 준비한 이 영상들로...')",
            userMood, tagsStr
        );

        // 3. API 요청 객체 조립
        GeminiRequest.Part part = new GeminiRequest.Part(promptText);
        GeminiRequest.Content content = new GeminiRequest.Content(List.of(part));
        GeminiRequest requestBody = new GeminiRequest(List.of(content));

        // 4. 헤더 설정 (API 키는 URL 쿼리 파라미터로도 넣을 수 있고 헤더로도 넣을 수 있습니다)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String requestUrl = geminiApiUrl + "?key=" + geminiApiKey;
        HttpEntity<GeminiRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 5. API 호출!
            GeminiResponse response = restTemplate.postForObject(requestUrl, requestEntity, GeminiResponse.class);
            
            // 6. 응답에서 알맹이 텍스트만 쏙 빼오기
            if (response != null && !response.candidates().isEmpty()) {
                return response.candidates().get(0).content().parts().get(0).text();
            }
            return "오늘 하루도 수고 많으셨어요. O+T가 추천하는 영상과 함께 편안한 시간 보내세요."; // Fallback 멘트
            
        } catch (Exception e) {
            System.err.println("Gemini API 호출 실패: " + e.getMessage());
            return "추천 영상과 함께 기분 좋은 시간 보내시길 바랄게요!"; // 서버 에러 시 기본 멘트
        }
    }
}
