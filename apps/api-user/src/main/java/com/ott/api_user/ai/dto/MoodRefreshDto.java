package com.ott.api_user.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

// fastapi와 통신할 dto
public class MoodRefreshDto {

    @Getter
    @AllArgsConstructor
    public static class Request {
        @JsonProperty("member_id")
        private Long memberId;

        @JsonProperty("input_tags")
        private List<String> inputTags;
    }

    @Getter
    @NoArgsConstructor
    public static class Response {
        @JsonProperty("target_tag_codes")
        private List<String> targetTagCodes;
    }
}