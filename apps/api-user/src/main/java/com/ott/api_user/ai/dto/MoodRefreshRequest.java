package com.ott.api_user.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MoodRefreshRequest {
    @JsonProperty("inputTags")
    private List<String> inputTags;
}