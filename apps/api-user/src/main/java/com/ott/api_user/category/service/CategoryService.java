package com.ott.api_user.category.service;

import com.ott.api_user.category.dto.response.CategoryResponse;
import com.ott.domain.category.repository.CategoryRepository;
import com.ott.domain.common.Status;
import com.ott.domain.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAllByStatus(Status.ACTIVE)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
