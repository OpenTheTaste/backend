package com.ott.api_user.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MoodRefreshResponse { 
    @JsonProperty("outputTags")
    private List<String> outputTags;
}