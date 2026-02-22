package com.ott.api_admin.shortform.mapper;

import com.ott.api_admin.shortform.dto.ShortFormListResponse;
import com.ott.domain.media.domain.Media;
import org.springframework.stereotype.Component;

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
}
