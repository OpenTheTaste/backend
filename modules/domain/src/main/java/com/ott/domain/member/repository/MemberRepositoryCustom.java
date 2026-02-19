package com.ott.domain.member.repository;

import com.ott.domain.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

    Page<Member> findMemberList(Pageable pageable);
}
