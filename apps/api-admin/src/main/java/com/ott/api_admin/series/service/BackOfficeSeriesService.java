package com.ott.api_admin.series.service;

import com.ott.api_admin.series.dto.response.SeriesListResponse;
import com.ott.domain.series.repository.SeriesRepository;
import com.ott.domain.series_tag.repository.SeriesTagRepository;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.series.domain.Series;
import com.ott.domain.series_tag.domain.SeriesTag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BackOfficeSeriesService {

    private final SeriesRepository seriesRepository;
    private final SeriesTagRepository seriesTagRepository;

    @Transactional(readOnly = true)
    public PageResponse<SeriesListResponse> getSeries(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        // 1. 시리즈 대상 페이징
        Page<Series> seriesPage = seriesRepository.findAll(pageable);

        // 2. 조회된 시리즈 ID 목록 추출
        List<Long> seriesIdList = seriesPage.getContent().stream()
                .map(Series::getId)
                .toList();

        // 3. IN절로 태그 일괄 조회
        Map<Long, List<SeriesTag>> tagListBySeriesId = seriesTagRepository
                .findWithTagAndCategoryBySeriesIds(seriesIdList).stream()
                .collect(Collectors.groupingBy(st -> st.getSeries().getId()));

        List<SeriesListResponse> responseList = seriesPage.getContent().stream()
                .map(series -> toResponse(series, tagListBySeriesId.getOrDefault(series.getId(), List.of())))
                .toList();

        PageInfo pageInfo = PageInfo.toPageInfo(
                seriesPage.getNumber(),
                seriesPage.getTotalPages(),
                seriesPage.getSize()
        );
        return PageResponse.toPageResponse(pageInfo, responseList);
    }

    private SeriesListResponse toResponse(Series series, List<SeriesTag> seriesTagList) {
        String categoryName = seriesTagList.stream()
                .findFirst()
                .map(st -> st.getTag().getCategory().getName())
                .orElse(null);

        List<String> tagNameList = seriesTagList.stream()
                .map(st -> st.getTag().getName())
                .toList();

        return new SeriesListResponse(
                series.getId(),
                series.getThumbnailUrl(),
                series.getTitle(),
                categoryName,
                tagNameList,
                series.getPublicStatus()
        );
    }
}
