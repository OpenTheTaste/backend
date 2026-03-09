package com.ott.api_user.media_matrics.dto.response;

import com.ott.domain.member_radar_preference.domain.MemberRadarPreference;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "레이더 차트 설정값 응답")
public class RadarPreferenceResponse {

    @Schema(description = "대중성", example = "80")
    private final Integer popularity;

    @Schema(description = "몰입도", example = "60")
    private final Integer immersion;

    @Schema(description = "마니아", example = "20")
    private final Integer mania;

    @Schema(description = "최신성", example = "90")
    private final Integer recency;

    @Schema(description = "재시청률", example = "10")
    private final Integer reWatch;

    public static RadarPreferenceResponse from(MemberRadarPreference preference) {
        return RadarPreferenceResponse.builder()
                .popularity(preference.getPopularity())
                .immersion(preference.getImmersion())
                .mania(preference.getMania())
                .recency(preference.getRecency())
                .reWatch(preference.getReWatch())
                .build();
    }
}
