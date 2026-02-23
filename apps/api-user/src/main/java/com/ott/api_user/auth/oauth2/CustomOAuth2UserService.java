package com.ott.api_user.auth.oauth2;

import com.ott.api_user.auth.oauth2.userinfo.KakaoUserInfo;
import com.ott.api_user.auth.service.KakaoAuthService;
import com.ott.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    /**
     * 사용자가 카카오 로그인화면에서 로그인
     * 카카오 인증 서버에서 인가코드와 함께 리다이렉트
     * 스프링 시큐리티의 OAuth2LoginAuthenicationFilter가 인가코드를 token-url에 전달하여 Access token 교환 (자동 구현)
     * DefaultOAuthUserService에서 기본적으로 loadUser를 호출하여 user-info-uri을 통하여 유저 객체인 oAuth2User를 생성
     */
    private final KakaoAuthService kakaoAuthService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // OAuth2UserRequest -> 어떤 클라이이언트, provider가 저장됨

        // 로그인된 객체는 로그인 정보가 없음
        // loadUser를 통해서 info-url를 통해 attributes를 채운 OAuth2User를 만듬
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 카카오 응답 객체 파싱
        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());

        // DB 조회
        Member member = kakaoAuthService.findOrCreateMember(kakaoUserInfo);

        // 신규 회원 판별
        boolean isNewMember = kakaoAuthService.isNewMember(member.getId());

        // attribute에 memberId(PK)와 신규 유저 유무를 적재
        // payload memberId, isNewMember만 들어감 -> 민감한 정보 적재 x
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("memberId", member.getId());
        attributes.put("isNewMember", isNewMember);

        // 스프링 시큐리티에 넘길 객체 반환 이때 권한은 ROLE_MEMBER임
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(member.getRole().getKey())),
                attributes,
                "id"
        );
    }
}
