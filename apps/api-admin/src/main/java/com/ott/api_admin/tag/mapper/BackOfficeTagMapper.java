package com.ott.api_admin.tag.mapper;

import com.ott.api_admin.tag.dto.response.TagViewResponse;
import com.ott.domain.watch_history.repository.TagViewCountProjection;
import org.springframework.stereotype.Component;

@Component
public class BackOfficeTagMapper {

    public TagViewResponse toTagViewResponse(TagViewCountProjection projection) {
        return new TagViewResponse(
                projection.tagName(),
                projection.viewCount()
        );
    }
}
