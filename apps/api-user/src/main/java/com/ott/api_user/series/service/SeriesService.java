package com.ott.api_user.series.service;

import java.util.List;
import java.util.Locale.Category;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.ott.api_user.series.dto.SeriesContentsResponse;
import com.ott.api_user.series.dto.SeriesDetailResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.bookmark.domain.Bookmark;
import com.ott.domain.bookmark.repository.BookmarkRepository;
import com.ott.domain.category.repository.CategoryRepository;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.common.Status;
import com.ott.domain.common.TargetType;
import com.ott.domain.contents.domain.Contents;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.likes.domain.Likes;
import com.ott.domain.likes.repository.LikesRepository;
import com.ott.domain.playback.domain.Playback;
import com.ott.domain.playback.repository.PlaybackRepository;
import com.ott.domain.series.domain.Series;
import com.ott.domain.series.repository.SeriesRepository;
import com.ott.domain.tag.repository.TagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeriesService {
        private final SeriesRepository seriesRepository;
        private final ContentsRepository contentsRepository;
        private final TagRepository tagRepository;
        private final CategoryRepository categoryRepository;
        private final BookmarkRepository bookmarkRepository;
        private final LikesRepository likesRepository;
        private final PlaybackRepository playbackRepository;

        // 시리즈 상세 조회
        public SeriesDetailResponse getSeriesDetail(Long seriesId, Long memberId) {
                Series series = seriesRepository
                                .findByIdAndStatusAndPublicStatus(seriesId, Status.ACTIVE, PublicStatus.PUBLIC)
                                .orElseThrow(() -> new BusinessException(ErrorCode.SERIES_NOT_FOUND));

                List<String> tags = tagRepository.findTagNamesBySeriesId(seriesId);
                List<String> categories = categoryRepository.findCategoryNameBySeriesId(seriesId);

                Boolean isBookmarked = bookmarkRepository.existsByMemberIdAndTargetIdAndTargetTypeAndStatus(memberId,
                                seriesId, TargetType.SERIES, Status.ACTIVE);

                Boolean isLiked = likesRepository.existsByMemberIdAndTargetIdAndTargetTypeAndStatus(memberId, seriesId,
                                TargetType.SERIES, Status.ACTIVE);

                return SeriesDetailResponse.builder()
                                .id(series.getId())
                                .title(series.getTitle())
                                .description(series.getDescription())
                                .actors(series.getActors())
                                .posterUrl(series.getPosterUrl())
                                .thumbnailUrl(series.getThumbnailUrl())
                                .category(categories.isEmpty() ? null : categories.get(0))
                                .tags(tags)
                                .isBookmarked(isBookmarked)
                                .isLiked(isLiked)
                                .build();
        }

        // 시리즈 콘텐츠 목록 조회 (페이징)
        public PageResponse getSeriesContents(Long seriesId, int page, int size, Long memberId) {
                Series series = seriesRepository
                                .findByIdAndStatusAndPublicStatus(seriesId, Status.ACTIVE, PublicStatus.PUBLIC)
                                .orElseThrow(() -> new BusinessException(ErrorCode.SERIES_NOT_FOUND));

                Pageable pageable = PageRequest.of(page, size);
                Page<Contents> contentsPage = contentsRepository.findBySeriesIdAndStatusAndPublicStatusOrderByIdAsc(
                                seriesId, Status.ACTIVE, PublicStatus.PUBLIC, pageable);

                List<SeriesContentsResponse> contentsList = contentsPage.getContent().stream().map(content -> {
                        Integer positionSec = 0;
                        if (memberId != null) {
                                positionSec = playbackRepository
                                                .findByMemberIdAndContentsIdAndStatus(memberId, content.getId(),
                                                                Status.ACTIVE)
                                                .map(Playback::getPositionSec)
                                                .orElse(0);
                        }

                        return SeriesContentsResponse.builder()
                                        .id(content.getId())
                                        .title(content.getTitle())
                                        .description(content.getDescription())
                                        .duration(content.getDuration())
                                        .thumbnailUrl(content.getThumbnailUrl())
                                        .positionSec(positionSec)
                                        .build();
                }).collect(Collectors.toList());

                PageInfo pageInfo = PageInfo.builder()
                                .currentPage(contentsPage.getNumber())
                                .totalPage(contentsPage.getTotalPages())
                                .pageSize(contentsPage.getSize())
                                .build();

                return PageResponse.toPageResponse(pageInfo, contentsList);
        }
}
