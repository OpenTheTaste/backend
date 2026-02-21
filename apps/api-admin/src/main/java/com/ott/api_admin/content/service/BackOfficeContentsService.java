package com.ott.api_admin.content.service;

import com.ott.api_admin.content.dto.response.ContentsDetailResponse;
import com.ott.api_admin.content.dto.response.ContentsListResponse;
import com.ott.api_admin.content.mapper.BackOfficeContentsMapper;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.common.MediaType;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.contents.domain.Contents;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.media_tag.domain.MediaTag;
import com.ott.domain.media_tag.repository.MediaTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BackOfficeContentsService {

    private final BackOfficeContentsMapper backOfficeContentsMapper;

    private final MediaRepository mediaRepository;
    private final MediaTagRepository mediaTagRepository;
    private final ContentsRepository contentsRepository;

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

    @Transactional(readOnly = true)
    public ContentsDetailResponse getContentsDetail(Long mediaId) {
        // 1. Contents + Media + Uploader + Series + Series.media 한 번에 조회
        Contents contents = contentsRepository.findWithMediaAndUploaderByMediaId(mediaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));

        Media media = contents.getMedia();
        String uploaderNickname = media.getUploader().getNickname();

        // 2. 소속 시리즈 제목 및 태그 추출
        Long originMediaId = mediaId;
        String seriesTitle = null;
        if (contents.getSeries() != null) {
            Media originMedia = contents.getSeries().getMedia();
            originMediaId = originMedia.getId();
            seriesTitle = originMedia.getTitle();
        }

        // 3. 태그 조회
        List<MediaTag> mediaTagList = mediaTagRepository.findWithTagAndCategoryByMediaId(originMediaId);

        return backOfficeContentsMapper.toContentsDetailResponse(contents, media, uploaderNickname, seriesTitle, mediaTagList);
    }
}
