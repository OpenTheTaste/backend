package com.ott.domain.member.repository;

import com.ott.domain.member.domain.Member;
import com.ott.domain.member.domain.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    // 기존 회원인지 신규 회원인지 DB 조회
    Optional<Member> findByProviderAndProviderId(Provider provider, String providerId);

    // 관리자&에디터용 조회
    Optional<Member> findByEmailAndProvider(String email, Provider provider);
}
