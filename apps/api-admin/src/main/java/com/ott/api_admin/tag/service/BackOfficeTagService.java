package com.ott.api_admin.tag.service;

import com.ott.api_admin.tag.dto.response.TagResponse;
import com.ott.api_admin.tag.dto.response.TagViewResponse;
import com.ott.api_admin.tag.mapper.BackOfficeTagMapper;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.category.domain.Category;
import com.ott.domain.category.repository.CategoryRepository;
import com.ott.domain.tag.domain.Tag;
import com.ott.domain.tag.repository.TagRepository;
import com.ott.domain.watch_history.repository.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BackOfficeTagService {

    private final BackOfficeTagMapper backOfficeTagMapper;

    private final WatchHistoryRepository watchHistoryRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<TagViewResponse> getTagViewStats(Long categoryId) {
        LocalDateTime startDate = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endDate = startDate.plusMonths(1);

        return watchHistoryRepository.countByTagAndCategoryIdAndWatchedBetween(categoryId, startDate, endDate)
                .stream()
                .map(backOfficeTagMapper::toTagViewResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getTagListByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        return tagRepository.findAllByCategory(category).stream()
                .map(backOfficeTagMapper::toTagResponse)
                .toList();
    }
}
