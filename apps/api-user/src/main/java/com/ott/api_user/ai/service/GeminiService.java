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
        
        // 2. 프롬프트 엔지니어링
        String promptText = String.format(
            "너는 트렌디한 스트리밍 앱 'O+T'의 메인 카피라이터야. " +
            "유저의 최근 시청 분위기는 '%s'이고, 분위기 전환용 추천 태그는 '%s'야. " +
            "아래 [기본 구조]를 따르되, 유저의 시청 분위기에 맞춰 괄호 [ ] 안의 내용을 매번 센스 있게 변형해서 카피를 작성해줘.\n" +
            "[기본 구조]: \"최근 [A] 시청 이력을 보셨군요. [B] 오늘, [C]\"\n" +
            "작성 규칙:\n" +
            "1. [A]: 유저의 최근 분위기를 요약하는 형용사 (예: 먹먹한, 도파민 터지는, 숨 막히는 등)\n" +
            "2. [B]: [A]를 바탕으로 유저의 현재 기분이나 오늘 하루를 유추한 수식어 (예: 생각이 많은, 텐션 올리고 싶은, 머리 비우고 싶은 등)\n" +
            "3. [C]: 추천 태그를 활용해 클릭을 유도하는 힙하고 짧은 추천 문구\n" +
            "제한 사항:\n" +
            "- 전체 글자 수는 70자 이내로 2문장을 넘지 않게 아주 짧게 쓸 것.\n" +
            "- '안녕하세요', 'O+T입니다', '위로', '진심' 같은 오글거리는 단어나 인사말 절대 금지.\n" +
            "출력 예시 (참고만 하고 상황에 맞춰 다르게 창작할 것):\n" +
            "\"최근 숨 막히는 시청 이력을 보셨군요. 긴장 좀 풀고 싶은 오늘, 가벼운 웃음이 터지는 티키타카 영상은 어때요?\"",
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
