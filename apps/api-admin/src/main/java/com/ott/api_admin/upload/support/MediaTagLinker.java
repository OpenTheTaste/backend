package com.ott.api_admin.upload.support;

import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.category.domain.Category;
import com.ott.domain.category.repository.CategoryRepository;
import com.ott.domain.common.Status;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media_tag.domain.MediaTag;
import com.ott.domain.media_tag.repository.MediaTagRepository;
import com.ott.domain.tag.domain.Tag;
import com.ott.domain.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MediaTagLinker {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final MediaTagRepository mediaTagRepository;

    public void linkTags(Media media, String categoryName, List<String> tagNameList) {
        if (media == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "태그를 연결할 미디어 정보가 없습니다.");
        }
        String normalizedCategoryName = normalizeName(categoryName);
        Set<String> normalizedTagNameSet = normalizeTagNames(tagNameList);

        Category category = categoryRepository.findByNameAndStatus(normalizedCategoryName, Status.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "유효한 카테고리를 찾을 수 없습니다."));

        List<Tag> tagList = tagRepository.findAllByCategoryIdAndNameInAndStatus(
                category.getId(),
                normalizedTagNameSet,
                Status.ACTIVE
        );

        //빠른 점검: 기존 태그 갯수와 db조회 갯수 비교
        //다르다 -> db에 없는 태그가 있음.
        if (tagList.size() != normalizedTagNameSet.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "카테고리에 맞지 않거나 존재하지 않는 태그가 포함되어 있습니다.");
        }

        List<MediaTag> mediaTagList = tagList.stream()
                .map(tag -> MediaTag.builder()
                        .media(media)
                        .tag(tag)
                        .build())
                .toList();
        mediaTagRepository.saveAll(mediaTagList);
    }

    private String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "카테고리/태그 이름은 비어 있을 수 없습니다.");
        }
        return value.trim();
    }

    private Set<String> normalizeTagNames(List<String> tagNameList) {
        if (tagNameList == null || tagNameList.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "태그 목록은 최소 1개 이상 필요합니다.");
        }

        Set<String> normalizedTagNameSet = new LinkedHashSet<>();
        for (String tagName : tagNameList) {
            String normalizedTagName = normalizeName(tagName);
            if (!normalizedTagNameSet.add(normalizedTagName)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "태그 목록에 중복된 값이 있습니다.");
            }
        }
        return normalizedTagNameSet;
    }
}
