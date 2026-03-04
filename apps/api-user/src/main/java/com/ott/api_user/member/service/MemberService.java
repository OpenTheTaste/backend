package com.ott.api_user.member.service;

import com.ott.api_user.auth.client.KakaoUnlinkClient;
import com.ott.api_user.member.dto.request.SetPreferredTagRequest;
import com.ott.api_user.member.dto.request.UpdateMemberRequest;
import com.ott.api_user.member.dto.response.*;
import com.ott.api_user.member.dto.response.TagMonthlyCompareResponse.MonthlyCount;
import com.ott.api_user.member.dto.response.TagRankingResponse.TagRankItem;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.bookmark.repository.BookmarkRepository;
import com.ott.domain.click_event.repository.ClickRepository;
import com.ott.domain.comment.repository.CommentRepository;
import com.ott.domain.common.Status;
import com.ott.domain.likes.repository.LikesRepository;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.domain.Provider;
import com.ott.domain.member.repository.MemberRepository;
import com.ott.domain.playback.repository.PlaybackRepository;
import com.ott.domain.preferred_tag.domain.PreferredTag;
import com.ott.domain.preferred_tag.repository.PreferredTagRepository;
import com.ott.domain.tag.domain.Tag;
import com.ott.domain.tag.repository.TagRepository;
import com.ott.domain.watch_history.repository.RecentWatchProjection;
import com.ott.domain.watch_history.repository.TagRankingProjection;
import com.ott.domain.watch_history.repository.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final MediaRepository mediaRepository;

    // 회원 탈퇴 시 soft delete
    private final KakaoUnlinkClient kakaoUnlinkClient;
    private final BookmarkRepository bookmarkRepository;
    private final LikesRepository likesRepository;
    private final PlaybackRepository playbackRepository;
    private final CommentRepository commentRepository;
    private final ClickRepository clickRepository;

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
        Member findMember = memberRepository.findByIdAndStatus(memberId, Status.ACTIVE)
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
        YearMonth currentYearMonth = YearMonth.now();
        LocalDateTime startDate = currentYearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate   = currentYearMonth.plusMonths(1).atDay(1).atStartOfDay(); // 다음 달 1일 00:00:00

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
    @Transactional(readOnly = true)
    public TagMonthlyCompareResponse getTagMonthlyCompare(Long memberId, Long tagId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Tag findTag = tagRepository.findByIdAndStatus(tagId, Status.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));

        // 이번 달 범위
        YearMonth currentYearMonth = YearMonth.now();
        LocalDateTime currentStart = currentYearMonth.atDay(1).atStartOfDay();
        LocalDateTime currentEnd   = currentYearMonth.plusMonths(1).atDay(1).atStartOfDay(); // 다음 달 1일 00:00:00

        // 저번 달 범위
        YearMonth prevYearMonth = currentYearMonth.minusMonths(1);
        LocalDateTime prevStart = prevYearMonth.atDay(1).atStartOfDay();
        LocalDateTime prevEnd   = currentYearMonth.atDay(1).atStartOfDay();  // 이번 달 1일 00:00:00

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

    // 태그별 추천 콘텐츠 목록 조회 (최대 20개)
    @Transactional(readOnly = true)
    public List<TagContentResponse> getRecommendContentsByTag(Long memberId, Long tagId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));

        return mediaRepository.findRecommendContentsByTagId(tagId, 20)
                .stream()
                .map(TagContentResponse::from)
                .toList();
    }

    // 전체 시청이력 플레이리스트 페이징 조회 (최신순, 10개씩)
    @Transactional(readOnly = true)
    public PageResponse<RecentWatchResponse> getWatchHistoryPlaylist(Long memberId, Integer page) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        PageRequest pageable = PageRequest.of(page, 10);

        Page<RecentWatchProjection> watchPage =
                watchHistoryRepository.findWatchHistoryByMemberId(memberId, pageable);

        List<RecentWatchResponse> dataList = watchPage.getContent()
                .stream()
                .map(RecentWatchResponse::from)
                .toList();

        PageInfo pageInfo = PageInfo.toPageInfo(
                watchPage.getNumber(),
                watchPage.getTotalPages(),
                watchPage.getSize()
        );

        return PageResponse.toPageResponse(pageInfo, dataList);
    }


    /**
     * 회원 탈퇴
     * 1. 카카오 회원인 경우 카카오 연결 끊기
     * 2. 연관 데이터 Soft Delete
     * 3. 회원 Soft Delete + refreshToken 초기화
     */
    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 1. 카카오 연결 끊기
        if (member.getProvider() == Provider.KAKAO) {
            kakaoUnlinkClient.unlink(member.getProviderId());
        }

        // 벌크 쿼리 이후 영속성 컨텍스트가 초기화 되기 때문에 member.withdraw 먼저 수행
        // JPA가 변경 감지를 못해서 순서 변경
        // 2. 회원 Soft Delete
        member.withdraw();

        // 탈퇴 회원의 ACTIVE한 북마크 수 차감
        bookmarkRepository.decreaseBookmarkCountByMemberId(memberId);
        bookmarkRepository.softDeleteAllByMemberId(memberId);


        // 탈퇴 회원의 ACTIVE한 좋아요 수 차감
        likesRepository.decreaseLikesCountByMemberId(memberId);
        likesRepository.softDeleteAllByMemberId(memberId);

        // 3. 연관 데이터 Soft Delete
        preferredTagRepository.softDeleteAllByMemberId(memberId);
        watchHistoryRepository.softDeleteAllByMemberId(memberId);
        playbackRepository.softDeleteAllByMemberId(memberId);
        commentRepository.softDeleteAllByMemberId(memberId);
        clickRepository.softDeleteAllByMemberId(memberId);
    }

    /**
     * 온보딩 건너뛰기 - onboardingCompleted = true 처리
     */
    @Transactional
    public void skipOnboarding(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        findMember.completeOnboarding();
    }
}
