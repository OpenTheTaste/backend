package com.ott.api_user.member.service;

import com.ott.api_user.member.dto.request.SetPreferredTagRequest;
import com.ott.api_user.member.dto.request.UpdateMemberRequest;
import com.ott.api_user.member.dto.response.MyPageResponse;
import com.ott.api_user.member.dto.response.TagMonthlyCompareResponse;
import com.ott.api_user.member.dto.response.TagMonthlyCompareResponse.MonthlyCount;
import com.ott.api_user.member.dto.response.TagRankingResponse;
import com.ott.api_user.member.dto.response.TagRankingResponse.TagRankItem;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.common.Status;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;
import com.ott.domain.preferred_tag.domain.PreferredTag;
import com.ott.domain.preferred_tag.repository.PreferredTagRepository;
import com.ott.domain.tag.domain.Tag;
import com.ott.domain.tag.repository.TagRepository;
import com.ott.domain.watch_history.repository.TagRankingProjection;
import com.ott.domain.watch_history.repository.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PreferredTagRepository preferredTagRepository;
    private final TagRepository tagRepository;
    private final WatchHistoryRepository watchHistoryRepository;

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
     * 온보딩 화면 : 초기 1회만 노출됨
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

    /**
     * 마이페이지 - 시청이력 기반 상위 태그 랭킹 조회 1달
     * - 상위 4개: 개별 태그 항목
     * - 나머지: count 합산하여 기타 항목으로 반환
     */
    @Transactional(readOnly = true)
    public TagRankingResponse getTagRanking(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 집계일과 마감일 선정 1일~말일까지
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1);

        List<TagRankingProjection> tagRankingProjections =
                watchHistoryRepository.findTopTagsByMemberIdAndWatchedBetween(memberId, startDate, endDate);

        List<TagRankItem> rankItems = new ArrayList<>();

        // 시청이력이 없을 경우 빈 리스트가 전달됨
        if (tagRankingProjections.isEmpty()) {
            return TagRankingResponse.builder().rankings(rankItems).build();
        }

        int topN = Math.min(4, tagRankingProjections.size());

        // 상위 4개 추가
        for (int i = 0; i < topN; i++) {
            TagRankingProjection projection = tagRankingProjections.get(i);
            rankItems.add(TagRankItem.of(projection.getTagId(), projection.getTagName(), projection.getCount()));
        }

        // 나머지 → 기타로 합산
        if (tagRankingProjections.size() > 4) {
            long etcCount = tagRankingProjections.subList(4, tagRankingProjections.size())
                    .stream()
                    .mapToLong(TagRankingProjection::getCount)
                    .sum();
            rankItems.add(TagRankItem.ofEtc(etcCount));
        }

        return TagRankingResponse.builder().rankings(rankItems).build();
    }


    /**
     * 마이페이지 - 특정 태그의 이번 달 vs 저번 달 시청 count 비교
     */
    public TagMonthlyCompareResponse getTagMonthlyCompare(Long memberId, Long tagId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Tag findTag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));

        // 이번 달 범위
        YearMonth currentYearMonth = YearMonth.now();
        LocalDateTime currentStart = currentYearMonth.atDay(1).atStartOfDay();
        LocalDateTime currentEnd   = currentYearMonth.atEndOfMonth().atTime(23, 59, 59);

        // 저번 달 범위
        YearMonth prevYearMonth = currentYearMonth.minusMonths(1);
        LocalDateTime prevStart = prevYearMonth.atDay(1).atStartOfDay();
        LocalDateTime prevEnd   = prevYearMonth.atEndOfMonth().atTime(23, 59, 59);

        Long currentCount  = watchHistoryRepository.countByMemberIdAndTagIdAndWatchedBetween(memberId, tagId, currentStart, currentEnd);
        Long previousCount = watchHistoryRepository.countByMemberIdAndTagIdAndWatchedBetween(memberId, tagId, prevStart, prevEnd);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        // 저번 달 시청 기록이 없으면 null
        MonthlyCount previousMonth = previousCount > 0
                ? MonthlyCount.builder()
                .yearMonth(prevYearMonth.format(formatter))
                .count(previousCount)
                .build()
                : null;

        return TagMonthlyCompareResponse.builder()
                .tagId(findTag.getId())
                .tagName(findTag.getName())
                .currentMonth(MonthlyCount.builder()
                        .yearMonth(currentYearMonth.format(formatter))
                        .count(currentCount)
                        .build())
                .previousMonth(previousMonth)
                .build();
    }
}
