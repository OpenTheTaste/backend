package com.ott.domain.member_radar_preference.repository;

import com.ott.domain.member_radar_preference.domain.MemberRadarPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRadarPreferenceRepository extends JpaRepository<MemberRadarPreference, Long> {

    Optional<MemberRadarPreference> findByMemberId(Long memberId);
}
