package com.ott.api_user.member.dto.response;

import com.ott.domain.watch_history.repository.RecentWatchProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "시청이력 플레이리스트 DTO")
public class RecentWatchResponse {

    @Schema(type = "Long", example = "3", description = "콘텐츠 ID (콘텐츠 상세 조회용)")
    private Long contentsId;

    @Schema(type = "String", example = "https://cdn.ott.com/poster/thriller01.jpg", description = "포스터 URL")
    private String posterUrl;

    @Schema(type = "Integer", example = "150", description = "이어보기 시점 (초), 없으면 0")
    private Integer positionSec;

    @Schema(type = "Integer", example = "3600", description = "전체 재생 시간 (초)")
    private Integer duration;

    public static RecentWatchResponse from(RecentWatchProjection projection) {
        return RecentWatchResponse.builder()
                .contentsId(projection.getContentsId())
                .posterUrl(projection.getPosterUrl())
                .positionSec(projection.getPositionSec())
                .duration(projection.getDuration())
                .build();
    }
}