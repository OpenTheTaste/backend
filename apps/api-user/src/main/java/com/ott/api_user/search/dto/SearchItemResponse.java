package com.ott.api_user.search.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ott.domain.common.MediaType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "검색 결과 항목 응답 DTO")
public class SearchItemResponse {
    @Schema(description = "미디어 타입 (콘텐츠 또는 시리즈)", example = "CONTENTS")
    private MediaType mediaType;

    @Schema(description = "콘텐츠 또는 시리즈의 고유 ID", example = "101")
    private Long mediaId;

    @Schema(description = "제목", example = "비밀의 숲")
    private String title;

    @Schema(description = "포스터 이미지 URL", example = "https://cdn.ott.com/posters/101.jpg")
    private String posterUrl;

}
