package com.ott.api_user.series.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional(readOnly = true) // 더티 체킹 비활성화
public class SeriesService {
        private final SeriesRepository seriesRepository;
        private final ContentsRepository contentsRepository;
        private final TagRepository tagRepository;
        private final CategoryRepository categoryRepository;
        private final BookmarkRepository bookmarkRepository;
        private final LikesRepository likesRepository;
        // private final PlaybackRepository playbackRepository;

        // 시리즈 상세 조회
        public SeriesDetailResponse getSeriesDetail(Long seriesId, Long memberId) {
                Series series = seriesRepository.findByIdWithMedia(seriesId, Status.ACTIVE, PublicStatus.PUBLIC)
                                .orElseThrow(() -> new BusinessException(ErrorCode.SERIES_NOT_FOUND));

                Long mediaId = series.getMedia().getId();

                List<String> tags = tagRepository.findTagNamesByMediaId(mediaId, Status.ACTIVE);
                List<String> categories = categoryRepository.findCategoryNamesByMediaId(mediaId, Status.ACTIVE);

                Boolean isBookmarked = bookmarkRepository.existsByMemberIdAndMediaIdAndStatus(memberId, mediaId,
                                Status.ACTIVE);
                Boolean isLiked = likesRepository.existsByMemberIdAndMediaIdAndStatus(memberId, mediaId, Status.ACTIVE);

                return SeriesDetailResponse.of(series, tags, categories, isBookmarked, isLiked);
        }

        // 시리즈 콘텐츠 목록 조회 (페이징)
        // 반환 타입 제네릭으로 수정
        public PageResponse<SeriesContentsResponse> getSeriesContents(Long seriesId, int page, int size,
                        Long memberId) {

                seriesRepository.findByIdWithMedia(seriesId, Status.ACTIVE, PublicStatus.PUBLIC)
                                .orElseThrow(() -> new BusinessException(ErrorCode.SERIES_NOT_FOUND));

                Pageable pageable = PageRequest.of(page, size);
                
                System.out.println("🚨 1. DB 쿼리 실행 직전!"); // <--- 추가
                
                Page<Contents> contentsPage = contentsRepository
                                .findBySeriesIdAndStatusAndMedia_PublicStatusOrderByIdAsc(
                                                seriesId, Status.ACTIVE, PublicStatus.PUBLIC, pageable);
                System.out.println("🚨 2. DB 조회 완료! (결과 개수: " + contentsPage.getContent().size() + ")"); // <--- 추가

                // List<SeriesContentsResponse> contentsList = contentsPage.getContent().stream()
                //                 .map(SeriesContentsResponse::from).collect(Collectors.toList());
                
                                
                // ======== 여기서부터 교체 ========
                List<SeriesContentsResponse> contentsList = new java.util.ArrayList<>();
                try {
                        for (Contents content : contentsPage.getContent()) {
                        System.out.println("👉 변환 시도 중인 에피소드 ID: " + content.getId());
                        contentsList.add(SeriesContentsResponse.from(content));
                        System.out.println("✅ 에피소드 ID: " + content.getId() + " 변환 성공!");
                        }
                } catch (Exception e) {
                        System.out.println("💥💥💥 대참사 발생! 범인은 바로 아래 에러입니다 💥💥💥");
                        e.printStackTrace(); // <--- 이게 에러의 진짜 원인을 콘솔에 뱉어줍니다!
                }
                // ======== 여기까지 교체 ========

                System.out.println("🚨 3. DTO 변환 및 리턴 직전!"); // <--- 추가
                PageInfo pageInfo = PageInfo.builder()
                                .currentPage(contentsPage.getNumber())
                                .totalPage(contentsPage.getTotalPages())
                                .pageSize(contentsPage.getSize())
                                .build();

                return PageResponse.toPageResponse(pageInfo, contentsList);
        }
}
