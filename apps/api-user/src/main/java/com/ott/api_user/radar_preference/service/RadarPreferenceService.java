package com.ott.api_user.radar_preference.service;

import com.ott.api_user.radar_preference.dto.request.RadarPreferenceRequest;
import com.ott.api_user.radar_preference.dto.response.RadarMediaResponse;
import com.ott.api_user.radar_preference.dto.response.RadarPreferenceResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media_metrics.repository.MediaMetricsRepository;
import com.ott.domain.member_radar_preference.domain.MemberRadarPreference;
import com.ott.domain.member_radar_preference.repository.MemberRadarPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ott.api_user.radar_preference.constant.RadarPreferenceConstant.DEFAULT_WEIGHT;
import static com.ott.api_user.radar_preference.constant.RadarPreferenceConstant.RECOMMEND_LIMIT;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RadarPreferenceService {

    private final MediaMetricsRepository mediaMetricsRepository;
    private final MemberRadarPreferenceRepository memberRadarPreferenceRepository;

    public RadarPreferenceResponse getPreference(Long memberId) {
        MemberRadarPreference preference = memberRadarPreferenceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RADAR_PREFERENCE_NOT_FOUND));

        return RadarPreferenceResponse.from(preference);
    }

    @Transactional
    public void updatePreference(Long memberId, RadarPreferenceRequest request) {
        MemberRadarPreference preference = memberRadarPreferenceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RADAR_PREFERENCE_NOT_FOUND));

        preference.updatePreference(
                request.getPopularity(),
                request.getImmersion(),
                request.getMania(),
                request.getRecency(),
                request.getReWatch()
        );
    }

    public List<RadarMediaResponse> getRecommendations(Long memberId) {
        MemberRadarPreference memberRadarPreference = memberRadarPreferenceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RADAR_PREFERENCE_NOT_FOUND));

        int popularity = memberRadarPreference.getPopularity();
        int immersion = memberRadarPreference.getImmersion();
        int mania = memberRadarPreference.getMania();
        int recency = memberRadarPreference.getRecency();
        int reWatch = memberRadarPreference.getReWatch();

        if (needDefaultWeight(popularity, immersion, mania, recency, reWatch)) {
            return List.of();
        }

        List<Media> mediaList = mediaMetricsRepository.findTopByWeightedScore(
                popularity, immersion, mania, recency, reWatch, RECOMMEND_LIMIT
        );

        return mediaList.stream()
                .map(RadarMediaResponse::from)
                .toList();
    }

    boolean needDefaultWeight(int popularity, int immersion, int mania, int recency, int reWatch) {
        return popularity == 0 && immersion == 0 && mania == 0 && recency == 0 && reWatch == 0;
    }
}
