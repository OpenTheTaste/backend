package com.ott.api_user.content.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.ott.api_user.content.dto.ContentsDetailResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.bookmark.repository.BookmarkRepository;
import com.ott.domain.category.repository.CategoryRepository;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.common.Status;
import com.ott.domain.contents.domain.Contents;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.likes.repository.LikesRepository;
import com.ott.domain.tag.repository.TagRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentsService {
        private final ContentsRepository contentsRepository;
        // private final PlaybackRepository playbackRepository;

        private final BookmarkRepository bookmarkRepository;
        private final LikesRepository likesRepository;
        private final TagRepository tagRepository;
        private final CategoryRepository categoryRepository;

        
        // 재생 상세
        public ContentsDetailResponse getContentDetail(Long mediaId, Long memberId) {
                Contents contents = contentsRepository.findByMediaIdAndStatusAndMedia_PublicStatus(mediaId, Status.ACTIVE, PublicStatus.PUBLIC)
                                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));

                List<String> tags = tagRepository.findTagNamesByMediaId(mediaId, Status.ACTIVE);
                List<String> categories = categoryRepository.findCategoryNamesByMediaId(mediaId, Status.ACTIVE);

                Boolean isBookmarked = bookmarkRepository.existsByMemberIdAndMediaIdAndStatus(memberId, mediaId,Status.ACTIVE);
                Boolean isLiked = likesRepository.existsByMemberIdAndMediaIdAndStatus(memberId, mediaId, Status.ACTIVE);

                String masterPlaylistUrl = contents.getMasterPlaylistUrl();

                Integer positionSec = 0;

                return ContentsDetailResponse.from(contents, tags, categories, isBookmarked, isLiked,masterPlaylistUrl, positionSec);
        }
}
