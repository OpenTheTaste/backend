package com.ott.api_admin.shortform.service;

import com.ott.api_admin.shortform.dto.ShortFormDetailResponse;
import com.ott.api_admin.shortform.dto.ShortFormListResponse;
import com.ott.api_admin.shortform.mapper.BackOfficeShortFormMapper;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.common.MediaType;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.media_tag.domain.MediaTag;
import com.ott.domain.media_tag.repository.MediaTagRepository;
import com.ott.domain.member.domain.Role;
import com.ott.domain.short_form.domain.ShortForm;
import com.ott.domain.short_form.repository.ShortFormRepository;
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
    private final MediaTagRepository mediaTagRepository;
    private final ShortFormRepository shortFormRepository;

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

    @Transactional(readOnly = true)
    public ShortFormDetailResponse getShortFormDetail(Long mediaId, Authentication authentication) {
        // 1. ShortForm + Media + Uploader + ShortForm.series or ShortForm.contents 한 번에 조회
        ShortForm shortForm = shortFormRepository.findWithMediaAndUploaderByMediaId(mediaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));

        // 2. 에디터 - 숏폼 업로더 권한 체크
        Long memberId = (Long) authentication.getPrincipal();
        boolean isEditor = authentication.getAuthorities().stream()
                .anyMatch(authority -> Role.EDITOR.getKey().equals(authority.getAuthority()));

        Media media = shortForm.getMedia();
        if (isEditor && !media.getUploader().getId().equals(memberId))
            throw new BusinessException(ErrorCode.FORBIDDEN);

        String uploaderNickname = media.getUploader().getNickname();

        // 2. 원본 미디어(시리즈 or 콘텐츠) 추출
        Media originMedia = shortForm.getOriginMedia();

        // 3. 태그 조회
        List<MediaTag> mediaTagList = mediaTagRepository.findWithTagAndCategoryByMediaId(originMedia.getId());

        return backOfficeShortFormMapper.toShortFormDetailResponse(shortForm, media, uploaderNickname, originMedia.getTitle(), mediaTagList);
    }
}
