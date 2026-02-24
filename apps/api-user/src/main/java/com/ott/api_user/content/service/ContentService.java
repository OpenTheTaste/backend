package com.ott.api_user.content.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ott.api_user.content.dto.ContentDetailResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.bookmark.repository.BookmarkRepository;
import com.ott.domain.category.repository.CategoryRepository;
import com.ott.domain.common.MediaType;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.common.Status;
import com.ott.domain.contents.domain.Contents;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.likes.repository.LikesRepository;
import com.ott.domain.media_tag.repository.MediaTagRepository;
import com.ott.domain.playback.repository.PlaybackRepository;
import com.ott.domain.tag.repository.TagRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentService {
    private final ContentsRepository contentsRepository;
    // private final PlaybackRepository playbackRepository;

    private final BookmarkRepository bookmarkRepository;
    private final LikesRepository likesRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;

    // 재생 상세
    public ContentDetailResponse getContentDetail(Long contentsId, Long memberId) {
        Contents contents = contentsRepository.findByIdWithMedia(contentsId, Status.ACTIVE, PublicStatus.PUBLIC)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERIES_NOT_FOUND));

        Long mediaId = contents.getMedia().getId();

        List<String> tags = tagRepository.findTagNamesByMediaId(mediaId, Status.ACTIVE);
        List<String> categories = categoryRepository.findCategoryNamesByMediaId(mediaId, Status.ACTIVE);

        Boolean isBookmarked = bookmarkRepository.existsByMemberIdAndMediaIdAndStatus(memberId, mediaId,
                Status.ACTIVE);
        Boolean isLiked = likesRepository.existsByMemberIdAndMediaIdAndStatus(memberId, mediaId, Status.ACTIVE);

        String masterPlaylistUrl = contents.getMasterPlaylistUrl();
        MediaType mediaType = MediaType.CONTENTS; // 재생 화면이므로 무조건 CONTENTS로 고정 (시리즈 아님)

        Integer positionSec = 0;

        return ContentDetailResponse.of(mediaType, contents, tags, categories, isBookmarked, isLiked, masterPlaylistUrl,
                positionSec);

    }

}
