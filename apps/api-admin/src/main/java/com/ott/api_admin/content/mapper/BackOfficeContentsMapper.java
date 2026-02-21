package com.ott.api_admin.content.mapper;

import com.ott.api_admin.content.dto.response.ContentsDetailResponse;
import com.ott.api_admin.content.dto.response.ContentsListResponse;
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
                media.getPosterUrl(),
                media.getTitle(),
                media.getPublicStatus(),
                media.getCreatedDate().toLocalDate()
        );
    }

    public ContentsDetailResponse toContentsDetailResponse(Contents contents, Media media, String uploaderNickname, String seriesTitle, List<MediaTag> mediaTagList) {
        String categoryName = extractCategoryName(mediaTagList);
        List<String> tagNameList = extractTagNameList(mediaTagList);

        return new ContentsDetailResponse(
                contents.getId(),
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
