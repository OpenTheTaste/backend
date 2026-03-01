package com.ott.api_user.playlist.dto.request;

import org.hibernate.annotations.SourceType;

import com.ott.api_user.common.ContentSource;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.media.MediaType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

// 요청 DTO
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "플레이 리스트 공통 요청 DTO")
public class PlaylistCondition {

    @Schema(description = "진입점 소스 타입", example = "USER, RECOMMEND, SEARCH 등 ..")
    private ContentSource contentSource;

    @Schema(description = "사용자 고유 ID", example = "1")
    private Long memberId;

    @Schema(description = "현재 컨텐츠의 Id", example = "1")
    private Long excludeMediaId;

    @Schema(description = "태그 고유 ID", example = "1")
    private Long tagId;

    @Schema(description = "미디어 타입", example = "SERIES, CONTENTS")
    private MediaType mediaType;

}
