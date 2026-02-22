package com.ott.api_admin.shortform.mapper;

import com.ott.api_admin.shortform.dto.ShortFormDetailResponse;
import com.ott.api_admin.shortform.dto.ShortFormListResponse;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media_tag.domain.MediaTag;
import com.ott.domain.short_form.domain.ShortForm;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BackOfficeShortFormMapper {

    public ShortFormListResponse toShortFormListResponse(Media media) {
        return new ShortFormListResponse(
                media.getId(),
                media.getPosterUrl(),
                media.getTitle(),
                media.getPublicStatus(),
                media.getCreatedDate().toLocalDate()
        );
    }

    public ShortFormDetailResponse toShortFormDetailResponse(ShortForm shortForm, Media media, String uploaderNickname, String originMediaTitle, List<MediaTag> mediaTagList) {
        String categoryName = extractCategoryName(mediaTagList);
        List<String> tagNameList = extractTagNameList(mediaTagList);

        return new ShortFormDetailResponse(
                shortForm.getId(),
                media.getPosterUrl(),
                media.getTitle(),
                media.getDescription(),
                originMediaTitle,
                uploaderNickname,
                shortForm.getDuration(),
                shortForm.getVideoSize(),
                categoryName,
                tagNameList,
                media.getPublicStatus(),
                media.getBookmarkCount(),
                media.getCreatedDate().toLocalDate()
        );
    }

    private String extractCategoryName(List<MediaTag> mediaTagList) {
        return mediaTagList.stream()
                .findFirst()
                .map(mt -> mt.getTag().getCategory().getName())
                .orElse(null);
    }

    private List<String> extractTagNameList(List<MediaTag> mediaTagList) {
        return mediaTagList.stream()
                .map(mt -> mt.getTag().getName())
                .toList();
    }
}
