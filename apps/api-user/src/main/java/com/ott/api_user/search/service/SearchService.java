package com.ott.api_user.search.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ott.api_user.search.dto.SearchItemResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.common.Status;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.series.domain.Series;
import com.ott.domain.contents.domain.Contents;
import com.ott.domain.series.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;

// 최신순 정렬을 위해 DB 페이징 방식 대신, 
// 검색 결과를 모두 가져와서 Java Stream으로 정렬 후, 페이지네이션 처리하는 방식으로 변경
// 추후 검색 대상이 늘어나거나 데이터 양이 많아질 경우, Querydsl 으로 검색 쿼리 최적화 필요!
@Service
@RequiredArgsConstructor
public class SearchService {
    private final ContentsRepository contentsRepository;
    private final SeriesRepository seriesRepository;

    public PageResponse search(String searchWord, int page, int size) {

        if (searchWord.length() < 2) {
            throw new BusinessException(ErrorCode.SEARCH_KEYWORD_TOO_SHORT);
        }

        // 사용자가 흔한 검색어 입력 시 너무 많은 데이터를 가져올 수 있으므로
        // 일단 최대 100개까지만 가져오도록 제한
        Pageable limit = PageRequest.of(0, 100);

        // 에피소드 제외, 시리즈와 단일 콘텐츠만 검색
        List<Contents> contentsList = contentsRepository.searchLatest(searchWord, Status.ACTIVE, limit);
        List<Series> seriesList = seriesRepository.searchLatest(searchWord, Status.ACTIVE, limit);

        // 컨텐츠+시리즈 통합 정렬
        List<SearchItemResponse> allResults = Stream.concat(
                contentsList.stream().map(c -> SearchItemResponse.builder()
                        .type("CONTENTS")
                        .id(c.getId())
                        .title(c.getTitle())
                        .posterUrl(c.getPosterUrl())
                        .createdAt(c.getCreatedDate())
                        .build()),
                seriesList.stream().map(s -> SearchItemResponse.builder()
                        .type("SERIES")
                        .id(s.getId())
                        .title(s.getTitle())
                        .posterUrl(s.getPosterUrl())
                        .createdAt(s.getCreatedDate())
                        .build()))
                .sorted(Comparator.comparing(SearchItemResponse::getCreatedAt).reversed()) // 통합 최신순 정렬
                .toList();

        // 페이징 계산 (직접 자르기)
        int totalElements = allResults.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int start = Math.min(page * size, totalElements);
        int end = Math.min(start + size, totalElements);

        List<SearchItemResponse> pagedResult = allResults.subList(start, end);

        PageInfo pageInfo = PageInfo.builder()
                .currentPage(page)
                .totalPage(totalPages)
                .pageSize(size)
                .build();

        return PageResponse.toPageResponse(pageInfo, pagedResult);
    }
}
