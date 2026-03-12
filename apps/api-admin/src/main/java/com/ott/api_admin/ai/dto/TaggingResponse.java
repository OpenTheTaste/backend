package com.ott.api_admin.ai.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TaggingResponse {
    @JsonProperty("mood_tags")
    private List<String> moodTags;
}
