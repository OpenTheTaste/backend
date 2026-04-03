package com.ott.api_admin.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaggingRequest {
    @JsonProperty("media_id")
    private Long mediaId;
    private String description;
}