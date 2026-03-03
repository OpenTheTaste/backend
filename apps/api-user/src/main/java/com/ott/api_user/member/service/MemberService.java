package com.ott.api_user.member.service;

import com.ott.api_user.member.dto.request.SetPreferredTagRequest;
import com.ott.api_user.member.dto.request.UpdateMemberRequest;
import com.ott.api_user.member.dto.response.MyPageResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.common.Status;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;
import com.ott.domain.preferred_tag.domain.PreferredTag;
import com.ott.domain.preferred_tag.repository.PreferredTagRepository;
import com.ott.domain.tag.domain.Tag;
import com.ott.domain.tag.repository.TagRepository;
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
    private final TagRepository tagRepository;

    /**
     * 마이 페이지 조회 : 닉네임, 선호태그 List 반환
     */
    public MyPageResponse getMyPage(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 건너뛰기한 유저는 빈리스트를 반환
        List<PreferredTag> preferredTags = preferredTagRepository.
                findAllWithTagAndCategoryByMemberIdAndStatus(memberId, Status.ACTIVE);

        return MyPageResponse.from(findMember, preferredTags);
    }


    /**
     * 마이페이지 내 정보 수정 : 닉네임, 선호태그 변경 후 반환
     */
    @Transactional
    public MyPageResponse updateMyInfo(Long memberId, UpdateMemberRequest request) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 닉네임 변경
        if (request.getNickname() != null) {
            if (request.getNickname().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "공백은 입력할 수 없습니다");
            }
            findMember.updateNickname(request.getNickname());
        }


        // 선호 태그 변경 -> 태그가 널인 경우? -> 일단 만들고 추가 예정
        // 1. 모든 태그 삭제
        if (request.getTagIds() != null) {
            preferredTagRepository.deleteAllByMember(findMember);

            // 새 태그 저장
            List<Tag> tags = tagRepository.findAllByIdInAndStatus(request.getTagIds(), Status.ACTIVE);
            if (tags.size() != request.getTagIds().size()) {
                throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
            }

            List<PreferredTag> newTags = tags.stream()
                    .map(tag -> PreferredTag.builder()
                            .member(findMember)
                            .tag(tag)
                            .build())
                    .toList();
            preferredTagRepository.saveAll(newTags);
        }

        // 변경 하고 최신 상태 유지
        List<PreferredTag> preferredTags = preferredTagRepository
                .findAllWithTagAndCategoryByMemberIdAndStatus(memberId, Status.ACTIVE);

        return MyPageResponse.from(findMember, preferredTags);
    }

    /**
     * 온보딩 화면 : 초기 1회만 노출되며
     */
    @Transactional
    public void setPreferredTags(Long memberId, SetPreferredTagRequest request) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        //재 호출 시 중복 방지 코드
        preferredTagRepository.deleteAllByMember(findMember);

        List<Tag> tags = tagRepository.findAllByIdInAndStatus(request.getTagsId(), Status.ACTIVE);
        if (tags.size() != request.getTagsId().size()) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }

        List<PreferredTag> preferredTags = tags.stream()
                .map(tag -> PreferredTag.builder()
                        .member(findMember)
                        .tag(tag)
                        .build())
                .toList();

        preferredTagRepository.saveAll(preferredTags);
        findMember.completeOnboarding();
    }

    }
