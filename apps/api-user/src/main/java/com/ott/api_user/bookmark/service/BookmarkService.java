package com.ott.api_user.bookmark.service;

import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.bookmark.domain.Bookmark;
import com.ott.domain.bookmark.repository.BookmarkRepository;
import com.ott.domain.common.Status;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
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
}
