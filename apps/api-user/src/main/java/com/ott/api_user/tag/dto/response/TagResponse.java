package com.ott.api_user.tag.dto.response;

import com.ott.domain.tag.domain.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "태그 응답 DTO")
public class TagResponse {

    @Schema(type = "Long", example = "1", description = "태그 고유 ID")
    private Long tagId;

    @Schema(type = "String", example = "로맨스", description = "태그명")
    private String name;

    public static TagResponse from(Tag tag) {
        return TagResponse.builder()
                .tagId(tag.getId())
                .name(tag.getName())
                .build();
    }
}
