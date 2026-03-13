package com.ott.domain.moodrefresh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ott.domain.moodrefresh.domain.MemberMoodRefresh;

public interface MemberMoodRefreshRepository extends JpaRepository<MemberMoodRefresh, Long>{

    // 특정 유저의 닫기 버튼을 누르지 않은 상태의 최신 카드 1건 조회
    Optional<MemberMoodRefresh> findTopByMemberIdAndIsHiddenFalseOrderByCreatedAtDesc(Long memberId);
} 