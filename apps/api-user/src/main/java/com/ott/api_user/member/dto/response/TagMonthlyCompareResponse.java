package com.ott.api_user.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "태그 월별 시청 count 비교 응답 DTO")
public class TagMonthlyCompareResponse {

    @Schema(type = "Long", example = "3", description = "태그 ID")
    private Long tagId;

    @Schema(type = "String", example = "스릴러", description = "태그명")
    private String tagName;

    @Schema(description = "이번 달 시청 count")
    private MonthlyCount currentMonth;

    @Schema(description = "저번 달 시청 count")
    private MonthlyCount previousMonth;

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "월별 시청 count 아이템")
    public static class MonthlyCount {

        @Schema(type = "String", example = "2026-03", description = "연월 (yyyy-MM)")
        private String yearMonth;

        @Schema(type = "Long", example = "12", description = "시청 횟수")
        private Long count;
    }
}