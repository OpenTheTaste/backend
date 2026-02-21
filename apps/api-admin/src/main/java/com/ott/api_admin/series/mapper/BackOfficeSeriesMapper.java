package com.ott.api_admin.series.mapper;

import com.ott.api_admin.series.dto.response.SeriesDetailResponse;
import com.ott.api_admin.series.dto.response.SeriesListResponse;
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

//    public SeriesDetailResponse toSeriesDetailResponse(Series series, List<SeriesTag> seriesTagList) {
//        String categoryName = extractCategoryName(seriesTagList);
//        List<String> tagNameList = extractTagNameList(seriesTagList);
//
//        return new SeriesDetailResponse(
//                series.getId(),
//                series.getTitle(),
//                series.getDescription(),
//                categoryName,
//                tagNameList,
//                series.getPublicStatus(),
//                series.getUploader().getNickname(),
//                series.getBookmarkCount(),
//                series.getActors(),
//                series.getPosterUrl(),
//                series.getThumbnailUrl()
//        );
//    }

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
