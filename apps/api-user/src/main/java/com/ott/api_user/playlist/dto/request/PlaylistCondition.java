package com.ott.api_user.playlist.dto.request;



import com.ott.api_user.common.ContentSource;
import com.ott.domain.common.MediaType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


// 플레이리스트 공통 요청 DTO
// 진입점, 플레이리스트 or 재생목록 (둘은 재사용, 현재 컨텐츠 id 에 따라) 에 따라 들어오는 파라미터를 공통으로 사용
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "플레이 리스트 공통 요청 DTO")
public class PlaylistCondition {

    @Schema(description = "진입점 소스 타입", example = "TRENDING, RECOMMEND, HISTORY 등 ..")
    private ContentSource contentSource;

    @Schema(description = "미디어 타입", example = "SERIES, CONTENTS")
    private MediaType mediaType;

    @Schema(description = "사용자 고유 ID", example = "1")
    private Long memberId;

    @Schema(description = "현재 컨텐츠의 Id", example = "1") // 상세 페이지 진입 시 재생목록에서 제외
    private Long excludeMediaId;

    @Schema(description = "태그 고유 ID", example = "1")
    private Long tagId;

    @Schema(description = "태그 랭킹 인덱스 (0, 1, 2)", example = "0")
    private Integer index;
}