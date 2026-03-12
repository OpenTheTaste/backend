package com.ott.api_admin.click_event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "숏폼 전환율 통계 응답")
public record ShortFormConversionResponse(
        @Schema(type = "double", description = "이번 달 전환율 (%)", example = "25.5")
        double thisMonthRate,

        @Schema(type = "double", description = "이번 달 전환율 - 지난달 전환율 (%p, 음수 및 0 가능)", example = "5.2")
        double rateDiff
) {
}
