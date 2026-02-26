package com.ott.api_user.bookmark.service;

import com.ott.api_user.bookmark.dto.response.BookmarkMediaResponse;
import com.ott.api_user.bookmark.dto.response.BookmarkShortFormResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.bookmark.domain.Bookmark;
import com.ott.domain.bookmark.repository.BookmarkRepository;
import com.ott.domain.common.MediaType;
import com.ott.domain.common.Status;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final MediaRepository mediaRepository;

    /**
     * 북마크 수정
     * 해당 유저의 ACTIVE 북마크가 있을 경우 -> 상태 DELETE + 카운트 감소
     * 해당 유저의 media의 북마크가 없을 경우 -> insert 상태 ACTIVE + 카운트 증가
     * 해당 유저의 media의 북마크가 있는데 DELETE 일 경우 -> ACTIVE 변경 + 카운트 증가
     */
    @Transactional
    public void editBookmark(Long memberId, Long mediaId) {

        Media findMedia = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDIA_NOT_FOUND));

        // 해당 유저가 해당 미디어에 대해서 북마크를 했는지 여부 체크
        bookmarkRepository.findByMemberIdAndMediaId(memberId, mediaId)
                .ifPresentOrElse(
                        bookmark -> {
                            // 이미 해당 미디어에 대해서 북마크한 경우 -> DELETE 변경 이후 + 카운트 감소
                            if (bookmark.getStatus() == Status.ACTIVE) {
                                bookmark.updateStatus(Status.DELETE);
                                findMedia.decreaseBookmarkCount();
                            } else {
                                // 해당 미디어에 대해 북마크를 안한 경우 -> ACTIVE 변경 이후 + 카운트 증가
                                bookmark.updateStatus(Status.ACTIVE);
                                findMedia.increaseBookmarkCount();
                            }
                        },
                        () -> {
                            // 행 자체가 없음 -> 신규임 -> insert이후  상태 ACTIVE + 카운트 증가
                            Member findMember = memberRepository.findById(memberId)
                                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                            bookmarkRepository.save(Bookmark.builder()
                                    .member(findMember)
                                    .media(findMedia)
                                    .build()); // 상태 default가 ACTIVE임

                            findMedia.increaseBookmarkCount();
                        }
                );
    }

    // 북마크 리스트 조회
    @Transactional(readOnly = true)
    public PageResponse<BookmarkMediaResponse> getBookmarkMediaList(Long memberId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        // 해당 유저의 ACTIVE인 북마크한 콘텐츠 OR 시리즈 타입 목록 페이징 조회 (fetch Join)
        Page<Bookmark> bookmarkPage =
                bookmarkRepository.findByMemberIdAndStatusAndMedia_MediaTypeInOrderByCreatedDateDesc(
                        memberId,
                        Status.ACTIVE,
                        List.of(MediaType.CONTENTS, MediaType.SERIES),
                        pageable
                );

        // Bookmark -> DTO로 변환
        List<BookmarkMediaResponse> dataList = bookmarkPage.getContent().stream()
                .map(BookmarkMediaResponse::from)
                .toList();

        // pageInfo 생성
        PageInfo pageInfo = PageInfo.toPageInfo(
                bookmarkPage.getNumber(),
                bookmarkPage.getTotalPages(),
                bookmarkPage.getSize()
        );

        return PageResponse.toPageResponse(pageInfo, dataList);
    }


    // 숏폼 리스트 조회
    @Transactional(readOnly = true)
    public PageResponse<BookmarkShortFormResponse> getBookmarkShortFormList(Long memberId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        // SHORT_FORM 타입만 조회
        Page<Bookmark> bookmarkPage = bookmarkRepository.findByMemberIdAndStatusAndMedia_MediaTypeOrderByCreatedDateDesc(
                memberId,
                Status.ACTIVE,
                MediaType.SHORT_FORM,
                pageable
        );

        // DTO 변환
        List<BookmarkShortFormResponse> dataList = bookmarkPage.getContent().stream()
                .map(BookmarkShortFormResponse::from)
                .toList();

        // pageInfo 생성
        PageInfo pageInfo = PageInfo.toPageInfo(
                bookmarkPage.getNumber(),
                bookmarkPage.getTotalPages(),
                bookmarkPage.getSize()
        );

        return PageResponse.toPageResponse(pageInfo, dataList);
    }
}
