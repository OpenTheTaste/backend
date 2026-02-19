package com.ott.api_admin.series.mapper;

import com.ott.api_admin.series.dto.response.SeriesDetailResponse;
import com.ott.api_admin.series.dto.response.SeriesListResponse;
import com.ott.domain.series.domain.Series;
import com.ott.domain.series_tag.domain.SeriesTag;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BackOfficeSeriesMapper {

    public SeriesListResponse toSeriesListResponse(Series series, List<SeriesTag> seriesTagList) {
        String categoryName = extractCategoryName(seriesTagList);
        List<String> tagNameList = extractTagNameList(seriesTagList);

        return new SeriesListResponse(
                series.getId(),
                series.getThumbnailUrl(),
                series.getTitle(),
                categoryName,
                tagNameList,
                series.getPublicStatus()
        );
    }

    public SeriesDetailResponse toSeriesDetailResponse(Series series, List<SeriesTag> seriesTagList) {
        String categoryName = extractCategoryName(seriesTagList);
        List<String> tagNameList = extractTagNameList(seriesTagList);

        return new SeriesDetailResponse(
                series.getId(),
                series.getTitle(),
                series.getDescription(),
                categoryName,
                tagNameList,
                series.getPublicStatus(),
                series.getUploader().getNickname(),
                series.getBookmarkCount(),
                series.getActors(),
                series.getPosterUrl(),
                series.getThumbnailUrl()
        );
    }

    private String extractCategoryName(List<SeriesTag> seriesTagList) {
        return seriesTagList.stream()
                .findFirst()
                .map(st -> st.getTag().getCategory().getName())
                .orElse(null);
    }

    private List<String> extractTagNameList(List<SeriesTag> seriesTagList) {
        return seriesTagList.stream()
                .map(st -> st.getTag().getName())
                .toList();
    }
}
