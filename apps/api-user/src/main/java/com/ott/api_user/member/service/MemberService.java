package com.ott.api_user.member.service;

import com.ott.api_user.member.dto.response.MyPageResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.common.Status;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;
import com.ott.domain.preferred_tag.domain.PreferredTag;
import com.ott.domain.preferred_tag.repository.PreferredTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PreferredTagRepository preferredTagRepository;


    public MyPageResponse getMyPage(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 건너뛰기한 유저는 빈리스트를 반환
        List<PreferredTag> preferredTags = preferredTagRepository.
                findAllWithTagAndCategoryByMemberIdAndStatus(memberId, Status.ACTIVE);

        return MyPageResponse.from(findMember, preferredTags);
    }

}
