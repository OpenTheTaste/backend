package com.ott.api_admin.content.service;

import com.ott.api_admin.content.dto.response.ContentsListResponse;
import com.ott.api_admin.content.mapper.BackOfficeContentsMapper;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.common.MediaType;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.media_tag.domain.MediaTag;
import com.ott.domain.media_tag.repository.MediaTagRepository;
import com.ott.domain.member.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BackOfficeContentsService {

    private final BackOfficeContentsMapper backOfficeContentsMapper;

    private final MediaRepository mediaRepository;

    @Transactional(readOnly = true)
    public PageResponse<ContentsListResponse> getContents(int page, int size, String searchWord, PublicStatus publicStatus) {
        Pageable pageable = PageRequest.of(page, size);

        // 미디어 중 콘텐츠 대상 페이징
        Page<Media> mediaPage = mediaRepository.findMediaListByMediaTypeAndSearchWordAndPublicStatus(pageable, MediaType.CONTENTS, searchWord, publicStatus);

        List<ContentsListResponse> responseList = mediaPage.getContent().stream()
                .map(backOfficeContentsMapper::toContentsListResponse)
                .toList();

        PageInfo pageInfo = PageInfo.toPageInfo(
                mediaPage.getNumber(),
                mediaPage.getTotalPages(),
                mediaPage.getSize()
        );
        return PageResponse.toPageResponse(pageInfo, responseList);
    }
}
