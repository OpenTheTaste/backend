package com.ott.domain.member.domain;

import com.ott.domain.common.BaseEntity;
import com.ott.domain.common.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Getter
@Table(name = "member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted;

    public static Member createKakaoMember(String providerId, String email, String nickname) {
        return Member.builder()
                .provider(Provider.KAKAO)
                .providerId(providerId)
                .email(email)
                .nickname(nickname)
                .role(Role.MEMBER)
                .build();
    }

    public void updateKakaoProfile(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
    }

    public void changeRole(Role targetRole) {
        if (!this.role.canTransitionTo(targetRole))
            throw new IllegalArgumentException("Invalid role transition: " + this.role + " -> " + targetRole);

        this.role = targetRole;
    }

    // 닉네임 변경
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // 온보딩 여부
    public void completeOnboarding() {
        this.onboardingCompleted = true;
    }

    // 회원 탈퇴 - Soft Delete (refreshToken 초기화 + status DELETE)
    public void withdraw() {
        this.refreshToken = null;
        this.updateStatus(Status.DELETE);
    }

    // 탈퇴(DELETE) 상태인 경우에만 ACTIVE로 복구
    public void reactivate() {
        if (this.getStatus() == Status.DELETE) {
            this.updateStatus(Status.ACTIVE);
        }
    }
}
