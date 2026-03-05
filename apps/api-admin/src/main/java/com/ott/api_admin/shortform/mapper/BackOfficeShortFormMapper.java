package com.ott.api_admin.shortform.mapper;

import com.ott.api_admin.shortform.dto.response.OriginMediaTitleListResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormDetailResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormListResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormUpdateResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormUploadResponse;
import com.ott.domain.common.MediaType;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media_tag.domain.MediaTag;
import com.ott.domain.short_form.domain.ShortForm;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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

    public OriginMediaTitleListResponse toOriginMediaTitleListResponse(
            Media media, Map<Long, Long> seriesIdByMediaId, Map<Long, Long> contentsIdByMediaId
    ) {
        Long originId = media.getMediaType() == MediaType.SERIES
                ? seriesIdByMediaId.get(media.getId())
                : contentsIdByMediaId.get(media.getId());

        return new OriginMediaTitleListResponse(
                originId,
                media.getTitle(),
                media.getMediaType()
        );
    }

    public ShortFormUploadResponse toShortFormUploadResponse(
            Long shortFormId,
            String posterObjectKey,
            String thumbnailObjectKey,
            String originObjectKey,
            String masterPlaylistObjectKey,
            String posterUploadUrl,
            String thumbnailUploadUrl,
            String originUploadUrl
    ) {
        return new ShortFormUploadResponse(
                shortFormId,
                posterObjectKey,
                thumbnailObjectKey,
                originObjectKey,
                masterPlaylistObjectKey,
                posterUploadUrl,
                thumbnailUploadUrl,
                originUploadUrl
        );
    }

    public ShortFormUpdateResponse toShortFormUpdateResponse(
            Long shortFormId,
            String posterObjectKey,
            String thumbnailObjectKey,
            String originObjectKey,
            String masterPlaylistObjectKey,
            String posterUploadUrl,
            String thumbnailUploadUrl,
            String originUploadUrl
    ) {
        return new ShortFormUpdateResponse(
                shortFormId,
                posterObjectKey,
                thumbnailObjectKey,
                originObjectKey,
                masterPlaylistObjectKey,
                posterUploadUrl,
                thumbnailUploadUrl,
                originUploadUrl
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
