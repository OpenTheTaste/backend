package com.ott.api_admin.content.mapper;

import com.ott.api_admin.content.dto.response.ContentsListResponse;
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
}
