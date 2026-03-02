package com.ott.api_user.auth.service;

import com.ott.api_user.auth.oauth2.userinfo.KakaoUserInfo;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.domain.Provider;
import com.ott.domain.member.repository.MemberRepository;
import com.ott.domain.preferred_tag.repository.PreferredTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 카카오의 로그인 회원 로직으로 다음과 같은 비즈니스 로직을 수행함
 * 회원 조회/생성
 * 프로필 동기화
 * 신규 회원 판별
 * Refresh Token 저장
 */
@Service
@RequiredArgsConstructor
@Transactional
public class KakaoAuthService {

    private final MemberRepository memberRepository;

    // 카카오 사용자 정보로 회원 조회 or 신규 생성
    // 기존 회원일 경우 프로필 동기화 필요
    public Member findOrCreateMember(KakaoUserInfo kakaoUserInfo) {
        return memberRepository
                .findByProviderAndProviderId(Provider.KAKAO, kakaoUserInfo.getProviderId())
                .map(existingMember -> {
                    existingMember.updateKakaoProfile(
                            kakaoUserInfo.getEmail(),
                            kakaoUserInfo.getNickname());
                    return existingMember;
                })
                .orElseGet(() -> memberRepository.save(
                        Member.createKakaoMember(
                                kakaoUserInfo.getProviderId(),
                                kakaoUserInfo.getEmail(),
                                kakaoUserInfo.getNickname())));
    }

    // 신규 회원 판별 -> 컬럼으로 판별
    public boolean isNewMember(Member member) {
        return !member.isOnboardingCompleted();
    }

    // refresh token 저장
    public void saveRefreshToken(Long memberId, String refreshToken) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        member.updateRefreshToken(refreshToken);
    }

}
