package com.ott.api_admin.series.service;

import com.ott.api_admin.series.dto.response.SeriesDetailResponse;
import com.ott.api_admin.series.dto.response.SeriesListResponse;
import com.ott.api_admin.series.mapper.BackOfficeSeriesMapper;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.common.MediaType;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.media_tag.domain.MediaTag;
import com.ott.domain.media_tag.repository.MediaTagRepository;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.series.domain.Series;
import com.ott.domain.series.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BackOfficeSeriesService {

    private final BackOfficeSeriesMapper backOfficeSeriesMapper;

    private final MediaRepository mediaRepository;
    private final MediaTagRepository mediaTagRepository;
    private final SeriesRepository seriesRepository;

    @Transactional(readOnly = true)
    public PageResponse<SeriesListResponse> getSeries(int page, int size, String searchWord) {
        Pageable pageable = PageRequest.of(page, size);

        // 1. 미디어 중 시리즈 대상 페이징
        Page<Media> mediaPage = mediaRepository.findMediaListByMediaType(pageable, MediaType.SERIES, searchWord);

        // 2. 조회된 미디어 ID 목록 추출
        List<Long> mediaIdList = mediaPage.getContent().stream()
                .map(Media::getId)
                .toList();

        // 3. IN절로 태그 일괄 조회
        Map<Long, List<MediaTag>> tagListByMediaId = mediaIdList.isEmpty()
                ? Collections.emptyMap()
                : mediaTagRepository.findWithTagAndCategoryByMediaIds(mediaIdList).stream()
                .collect(Collectors.groupingBy(mt -> mt.getMedia().getId()));

        List<SeriesListResponse> responseList = mediaPage.getContent().stream()
                .map(media -> backOfficeSeriesMapper.toSeriesListResponse(
                        media,
                        tagListByMediaId.getOrDefault(media.getId(), List.of())
                ))
                .toList();

        PageInfo pageInfo = PageInfo.toPageInfo(
                mediaPage.getNumber(),
                mediaPage.getTotalPages(),
                mediaPage.getSize()
        );
        return PageResponse.toPageResponse(pageInfo, responseList);
    }

    @Transactional(readOnly = true)
    public SeriesDetailResponse getSeriesDetail(Long mediaId) {
        // 1. Series + Media + Uploader 한 번에 조회
        Series series = seriesRepository.findWithMediaAndUploaderByMediaId(mediaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERIES_NOT_FOUND));

        Media media = series.getMedia();
        String uploaderNickname = media.getUploader().getNickname();

        // 2. 태그 조회
        List<MediaTag> mediaTagList = mediaTagRepository.findWithTagAndCategoryByMediaId(mediaId);

        return backOfficeSeriesMapper.toSeriesDetailResponse(series, media, uploaderNickname, mediaTagList);
    }
}
