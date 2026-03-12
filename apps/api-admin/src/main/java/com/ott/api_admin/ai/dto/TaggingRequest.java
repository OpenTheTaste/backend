package com.ott.api_admin.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaggingRequest {
    @JsonProperty("media_id")
    private Long mediaId;        // 에러 로깅이나 추적을 위해 남겨둠
    
    private String description;  // 영상 줄거리 (AI 분석의 핵심 재료)
}