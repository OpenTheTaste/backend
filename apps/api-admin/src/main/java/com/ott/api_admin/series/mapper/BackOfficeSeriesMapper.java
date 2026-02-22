package com.ott.api_admin.series.mapper;

import com.ott.api_admin.series.dto.response.SeriesDetailResponse;
import com.ott.api_admin.series.dto.response.SeriesListResponse;
import com.ott.api_admin.series.dto.response.SeriesTitleListResponse;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media_tag.domain.MediaTag;
import com.ott.domain.series.domain.Series;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BackOfficeSeriesMapper {

    public SeriesListResponse toSeriesListResponse(Media media, List<MediaTag> mediaTagList) {
        String categoryName = extractCategoryName(mediaTagList);
        List<String> tagNameList = extractTagNameList(mediaTagList);

        return new SeriesListResponse(
                media.getId(),
                media.getThumbnailUrl(),
                media.getTitle(),
                categoryName,
                tagNameList,
                media.getPublicStatus()
        );
    }

    public SeriesTitleListResponse toSeriesTitleList(Series series) {
        return new SeriesTitleListResponse(
                series.getId(),
                series.getMedia().getTitle()
        );
    }

    public SeriesDetailResponse toSeriesDetailResponse(Series series, Media media, String uploaderName, List<MediaTag> mediaTagList) {
        String categoryName = extractCategoryName(mediaTagList);
        List<String> tagNameList = extractTagNameList(mediaTagList);

        return new SeriesDetailResponse(
                series.getId(),
                media.getTitle(),
                media.getDescription(),
                categoryName,
                tagNameList,
                media.getPublicStatus(),
                uploaderName,
                media.getBookmarkCount(),
                series.getActors(),
                media.getPosterUrl(),
                media.getThumbnailUrl()
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
