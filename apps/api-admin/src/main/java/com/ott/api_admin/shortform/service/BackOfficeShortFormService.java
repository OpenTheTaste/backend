package com.ott.api_admin.shortform.service;

import com.ott.api_admin.shortform.dto.ShortFormListResponse;
import com.ott.api_admin.shortform.mapper.BackOfficeShortFormMapper;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.common.MediaType;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.member.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BackOfficeShortFormService {

    private final BackOfficeShortFormMapper backOfficeShortFormMapper;

    private final MediaRepository mediaRepository;

    @Transactional(readOnly = true)
    public PageResponse<ShortFormListResponse> getShortFormList(
            Integer page, Integer size, String searchWord, PublicStatus publicStatus, Authentication authentication
    ) {
        Pageable pageable = PageRequest.of(page, size);

        // 1. 관리자/에디터 여부 확인
        Long memberId = (Long) authentication.getPrincipal();
        boolean isEditor = authentication.getAuthorities().stream()
                .anyMatch(authority -> Role.EDITOR.getKey().equals(authority.getAuthority()));
        Long uploaderId = null;

        // 2. 에디터인 경우 본인이 업로드한 숏폼만 조회 가능
        if (isEditor)
            uploaderId = memberId;

        Page<Media> mediaPage = mediaRepository.findMediaListByMediaTypeAndSearchWordAndPublicStatusAndUploaderId(
                pageable, MediaType.SHORT_FORM, searchWord, publicStatus, uploaderId
        );

        List<ShortFormListResponse> responseList = mediaPage.getContent().stream()
                .map(backOfficeShortFormMapper::toShortFormListResponse)
                .toList();

        PageInfo pageInfo = PageInfo.toPageInfo(
                mediaPage.getNumber(),
                mediaPage.getTotalPages(),
                mediaPage.getSize()
        );

        return PageResponse.toPageResponse(pageInfo, responseList);
    }
}
