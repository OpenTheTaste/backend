package com.ott.api_user.media_matrics.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "레이더 차트 설정값 수정 요청")
public class RadarPreferenceRequest {

    @NotNull @Min(0) @Max(100)
    @Schema(description = "대중성", example = "80")
    private Integer popularity;

    @NotNull @Min(0) @Max(100)
    @Schema(description = "몰입도", example = "60")
    private Integer immersion;

    @NotNull @Min(0) @Max(100)
    @Schema(description = "마니아", example = "20")
    private Integer mania;

    @NotNull @Min(0) @Max(100)
    @Schema(description = "최신성", example = "90")
    private Integer recency;

    @NotNull @Min(0) @Max(100)
    @Schema(description = "재시청률", example = "10")
    private Integer reWatch;
}
