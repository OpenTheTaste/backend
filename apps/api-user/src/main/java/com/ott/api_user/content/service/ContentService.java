package com.ott.api_user.content.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.ott.api_user.common.ContentSource;
import com.ott.api_user.common.dto.ContentListElement;
import com.ott.api_user.content.dto.ContentDetailResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.bookmark.repository.BookmarkRepository;
import com.ott.domain.category.repository.CategoryRepository;
import com.ott.domain.common.MediaType;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.common.Status;
import com.ott.domain.contents.domain.Contents;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.likes.repository.LikesRepository;
import com.ott.domain.media.domain.Media;
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

                Integer positionSec = 0;

                return ContentDetailResponse.from(contents, tags, categories, isBookmarked, isLiked,
                                masterPlaylistUrl,
                                positionSec);

        }

        // 해당 콘텐츠를 어디서 진입했는지에 따라
        // 콘텐츠의 재생목록이 달라짐.
        public PageResponse<ContentListElement> getContentPlayList(Long contentsId, ContentSource source,
                        int page, int size, Long memberId) {

                Contents currentContents = contentsRepository.findByIdWithMedia(contentsId, Status.ACTIVE,
                                PublicStatus.PUBLIC)
                                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));

                if (currentContents.getSeries() != null) {
                        throw new BusinessException(ErrorCode.INVALID_REQUEST_FOR_SERIES_PLAYLIST);
                }

                Pageable pageable = PageRequest.of(page, size);
                ContentSource currentSource = (source != null) ? source : ContentSource.TRENDING;
                Long mediaId = currentContents.getMedia().getId();

                Page<Media> playListPage = switch (currentSource) {
                        // 진입점에 따른 재생 목록 노출 리스트 분기 로직
                        case TRENDING -> getTrendingPlaylist(pageable, mediaId); // 1. 북마크 많은 순
                        case HISTORY -> getHistoryPlaylist(memberId, pageable, mediaId); // 2. 최근 시청한 순 (신규)
                        case RECOMMEND -> getRecommendPlaylist(memberId, pageable, mediaId); // 3. OO 님이 좋아하실만한 리스트
                        case TAG -> getTagPlaylist(mediaId, pageable); // 4. 같은 태그 가진 영상
                        case BOOKMARK -> getBookmarkPlaylist(memberId, pageable, mediaId); // 5. 내 북마크 목록
                        default -> getRecommendPlaylist(memberId, pageable, mediaId); // 기본값은 OO님이 좋아하실만한 콘텐츠
                };

                List<ContentListElement> contentList = playListPage.getContent().stream()
                                .map(ContentListElement::from).collect(Collectors.toList());

                PageInfo pageInfo = PageInfo.builder()
                                .currentPage(playListPage.getNumber())
                                .totalPage(playListPage.getTotalPages())
                                .pageSize(playListPage.getSize())
                                .build();

                return PageResponse.toPageResponse(pageInfo, contentList);
        }

        // 현재는 switch 문을 활용해 직관적 분기 처리를 구현하였지만
        // 새로운 추천 로직이 추가될때마다 서비스코드가 길어질 것을 우려해서
        // Strategy Pattern 을 적용해 객체 지향적 코드로 리팩토링해야함!

        private Page<Media> getTrendingPlaylist(Pageable pageable, Long excludeMediaId) {
                return Page.empty();
        }

        private Page<Media> getHistoryPlaylist(Long memberId, Pageable pageable, Long excludeMediaId) {
                return Page.empty();
        }

        private Page<Media> getRecommendPlaylist(Long memberId, Pageable pageable, Long excludeMediaId) {
                return Page.empty();
        }

        private Page<Media> getTagPlaylist(Long targetMediaId, Pageable pageable) {
                // 태그는 '기준 미디어(targetMediaId)'의 태그를 찾으면서, 동시에 해당 미디어를 결과에서 제외해야 함.
                return Page.empty();
        }

        private Page<Media> getBookmarkPlaylist(Long memberId, Pageable pageable, Long excludeMediaId) {
                return Page.empty();
        }

}
