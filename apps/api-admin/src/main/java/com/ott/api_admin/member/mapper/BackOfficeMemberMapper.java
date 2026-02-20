package com.ott.api_admin.member.mapper;

import com.ott.api_admin.member.dto.response.MemberListResponse;
import com.ott.domain.member.domain.Member;
import org.springframework.stereotype.Component;

@Component
public class BackOfficeMemberMapper {

    public MemberListResponse toMemberListResponse(Member member) {
        return new MemberListResponse(
                member.getId(),
                member.getNickname(),
                member.getEmail(),
                member.getRole(),
                member.getCreatedDate().toLocalDate()
        );
    }
}
