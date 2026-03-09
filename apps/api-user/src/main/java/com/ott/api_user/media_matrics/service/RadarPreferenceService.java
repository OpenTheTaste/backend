package com.ott.api_user.media_matrics.service;

import com.ott.api_user.media_matrics.dto.request.RadarPreferenceRequest;
import com.ott.api_user.media_matrics.dto.response.RadarPreferenceResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.member_radar_preference.domain.MemberRadarPreference;
import com.ott.domain.member_radar_preference.repository.MemberRadarPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RadarPreferenceService {

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
}
