package com.ott.api_admin.member.service;

import com.ott.api_admin.member.dto.response.MemberListResponse;
import com.ott.api_admin.member.mapper.BackOfficeMemberMapper;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BackOfficeMemberService {

    private final BackOfficeMemberMapper backOfficeMemberMapper;

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public PageResponse<MemberListResponse> getMemberList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        Page<Member> memberPage = memberRepository.findAll(pageable);

        List<MemberListResponse> responseList = memberPage.getContent().stream()
                .map(backOfficeMemberMapper::toMemberListResponse)
                .toList();

        PageInfo pageInfo = PageInfo.toPageInfo(
                memberPage.getNumber(),
                memberPage.getTotalPages(),
                memberPage.getSize()
        );
        return PageResponse.toPageResponse(pageInfo, responseList);
    }
}
