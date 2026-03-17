package com.ott.api_admin.content.mapper;

import com.ott.api_admin.content.dto.response.ContentsDetailResponse;
import com.ott.api_admin.content.dto.response.ContentsListResponse;
import com.ott.api_admin.content.dto.response.ContentsUpdateResponse;
import com.ott.api_admin.content.dto.response.ContentsUploadResponse;
import com.ott.domain.contents.domain.Contents;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media_tag.domain.MediaTag;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BackOfficeContentsMapper {

    public ContentsListResponse toContentsListResponse(Media media) {
        return new ContentsListResponse(
                media.getId(),
                media.getThumbnailUrl(),
                media.getTitle(),
                media.getPublicStatus(),
                media.getCreatedDate().toLocalDate()
        );
    }

    public ContentsDetailResponse toContentsDetailResponse(Long seriesId, Contents contents, Media media, String uploaderNickname, String seriesTitle, List<MediaTag> mediaTagList) {
        String categoryName = extractCategoryName(mediaTagList);
        List<String> tagNameList = extractTagNameList(mediaTagList);

        return new ContentsDetailResponse(
                contents.getId(),
                seriesId,
                media.getPosterUrl(),
                media.getThumbnailUrl(),
                media.getTitle(),
                media.getDescription(),
                contents.getActors(),
                seriesTitle,
                uploaderNickname,
                contents.getDuration(),
                contents.getVideoSize(),
                categoryName,
                tagNameList,
                media.getPublicStatus(),
                media.getBookmarkCount(),
                media.getCreatedDate().toLocalDate()
        );
    }

    public ContentsUploadResponse toContentsUploadResponse(
            Long contentsId,
            String posterObjectKey,
            String thumbnailObjectKey,
            String originObjectKey,
            String masterPlaylistObjectKey,
            String posterUploadUrl,
            String thumbnailUploadUrl,
            String originUploadId,
            int originTotalPartCount,
            long originPartSizeBytes
    ) {
        return new ContentsUploadResponse(
                contentsId,
                posterObjectKey,
                thumbnailObjectKey,
                originObjectKey,
                masterPlaylistObjectKey,
                posterUploadUrl,
                thumbnailUploadUrl,
                originUploadId,
                originTotalPartCount,
                originPartSizeBytes
        );
    }

    public ContentsUpdateResponse toContentsUpdateResponse(
            Long contentsId,
            String posterObjectKey,
            String thumbnailObjectKey,
            String posterUploadUrl,
            String thumbnailUploadUrl
    ) {
        return new ContentsUpdateResponse(
                contentsId,
                posterObjectKey,
                thumbnailObjectKey,
                posterUploadUrl,
                thumbnailUploadUrl
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
