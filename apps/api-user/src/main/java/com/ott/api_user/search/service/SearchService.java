package com.ott.api_user.search.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ott.api_user.search.dto.SearchItemResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.common.Status;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final MediaRepository mediaRepository;

    public PageResponse search(String searchWord, int page, int size) {
        
        // 1. 파라미터 유효성 검증
        if (searchWord == null || searchWord.length() < 2) {
            throw new BusinessException(ErrorCode.SEARCH_KEYWORD_TOO_SHORT);
        }

        // 2. DB 레벨의 페이징 및 최신순 정렬 (생성일 기준 내림차순)
        Pageable pageable = PageRequest.of(page, size);
        
        // 3. 통합 검색 쿼리 실행
        Page<Media> mediaPage = mediaRepository.findUserSearchMediaList(pageable, searchWord);


        // 4. Entity -> DTO 변환
        List<SearchItemResponse> pagedResult = mediaPage.getContent().stream()
                .map(m -> SearchItemResponse.builder()
                         .mediatype(m.getMediaType())
                         .mediaId(m.getId())
                         .title(m.getTitle())
                         .posterUrl(m.getPosterUrl())
                         .build())
                .collect(Collectors.toList());

        // 5. 응답용 PageInfo 생성
        PageInfo pageInfo = PageInfo.builder()
                .currentPage(mediaPage.getNumber())
                .totalPage(mediaPage.getTotalPages())
                .pageSize(mediaPage.getSize())
                .build();

        return PageResponse.toPageResponse(pageInfo, pagedResult);
    }
}