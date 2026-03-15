package com.ott.api_admin.shortform.service;

import com.ott.api_admin.shortform.dto.response.OriginMediaTitleListResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormDetailResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormListResponse;
import com.ott.api_admin.shortform.mapper.BackOfficeShortFormMapper;
import com.ott.api_admin.upload.dto.response.MultipartUploadPartUrlResponse;
import com.ott.api_admin.upload.support.UploadHelper;
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
import com.ott.domain.member.domain.Role;
import com.ott.domain.series.domain.Series;
import com.ott.domain.series.repository.SeriesRepository;
import com.ott.domain.short_form.domain.ShortForm;
import com.ott.domain.short_form.repository.ShortFormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class BackOfficeShortFormReader {

    private final BackOfficeShortFormMapper backOfficeShortFormMapper;
    private final MediaRepository mediaRepository;
    private final MediaTagRepository mediaTagRepository;
    private final SeriesRepository seriesRepository;
    private final ContentsRepository contentsRepository;
    private final ShortFormRepository shortFormRepository;
    private final UploadHelper uploadHelper;

    @Transactional(readOnly = true)
    public PageResponse<ShortFormListResponse> getShortFormList(
            Integer page, Integer size, String searchWord, PublicStatus publicStatus,
            Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size);

        Long memberId = (Long) authentication.getPrincipal();
        boolean isEditor = authentication.getAuthorities().stream()
                .anyMatch(authority -> Role.EDITOR.getKey().equals(authority.getAuthority()));
        Long uploaderId = null;

        if (isEditor) {
            uploaderId = memberId;
        }

        Page<Media> mediaPage = mediaRepository
                .findMediaListByMediaTypeAndSearchWordAndPublicStatusAndUploaderId(
                        pageable, MediaType.SHORT_FORM, searchWord, publicStatus, uploaderId);

        List<ShortFormListResponse> responseList = mediaPage.getContent().stream()
                .map(backOfficeShortFormMapper::toShortFormListResponse)
                .toList();

        PageInfo pageInfo = PageInfo.toPageInfo(
                mediaPage.getNumber(),
                mediaPage.getTotalPages(),
                mediaPage.getSize());

        return PageResponse.toPageResponse(pageInfo, responseList);
    }

    @Transactional(readOnly = true)
    public PageResponse<OriginMediaTitleListResponse> getOriginMediaTitle(Integer page, Integer size, String searchWord) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Media> mediaPage = mediaRepository.findOriginMediaListBySearchWord(pageable, searchWord);

        List<Media> mediaList = mediaPage.getContent();

        List<Long> seriesMediaIdList = mediaList.stream()
                .filter(m -> m.getMediaType() == MediaType.SERIES)
                .map(Media::getId)
                .toList();

        List<Long> contentsMediaIdList = mediaList.stream()
                .filter(m -> m.getMediaType() == MediaType.CONTENTS)
                .map(Media::getId)
                .toList();

        Map<Long, Long> seriesIdByMediaId = seriesRepository.findAllByMediaIdIn(seriesMediaIdList).stream()
                .collect(Collectors.toMap(s -> s.getMedia().getId(), Series::getId));

        Map<Long, Long> contentsIdByMediaId = contentsRepository.findAllByMediaIdIn(contentsMediaIdList)
                .stream()
                .collect(Collectors.toMap(c -> c.getMedia().getId(), Contents::getId));

        List<OriginMediaTitleListResponse> responseList = mediaList.stream()
                .map(m -> backOfficeShortFormMapper.toOriginMediaTitleListResponse(m, seriesIdByMediaId,
                        contentsIdByMediaId))
                .toList();

        PageInfo pageInfo = PageInfo.toPageInfo(
                mediaPage.getNumber(),
                mediaPage.getTotalPages(),
                mediaPage.getSize());

        return PageResponse.toPageResponse(pageInfo, responseList);
    }

    @Transactional(readOnly = true)
    public ShortFormDetailResponse getShortFormDetail(Long mediaId, Authentication authentication) {
        ShortForm shortForm = shortFormRepository.findWithMediaAndUploaderByMediaId(mediaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHORT_FORM_NOT_FOUND));

        Long memberId = (Long) authentication.getPrincipal();
        boolean isEditor = authentication.getAuthorities().stream()
                .anyMatch(authority -> Role.EDITOR.getKey().equals(authority.getAuthority()));

        Media media = shortForm.getMedia();
        if (isEditor && !media.getUploader().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        String uploaderNickname = media.getUploader().getNickname();

        Optional<Media> originMedia = shortForm.findOriginMedia();
        String originMediaTitle = null;
        Long originId = null;
        MediaType originType = null;
        if (originMedia.isPresent()) {
            originMediaTitle = originMedia.get().getTitle();
            if (shortForm.getSeries() != null) {
                originId = shortForm.getSeries().getId();
                originType = MediaType.SERIES;
            } else if (shortForm.getContents() != null) {
                originId = shortForm.getContents().getId();
                originType = MediaType.CONTENTS;
            }
        }

        List<MediaTag> mediaTagList = mediaTagRepository.findWithTagAndCategoryByMediaId(mediaId);

        return backOfficeShortFormMapper.toShortFormDetailResponse(
                shortForm,
                media,
                uploaderNickname,
                originMediaTitle,
                originId,
                originType,
                mediaTagList
        );
    }

    @Transactional(readOnly = true)
    public int getShortFormUploadInfo(Long shortFormId, String objectKey, Authentication authentication) {
        ShortForm shortForm = shortFormRepository.findWithMediaAndUploaderByShortFormId(shortFormId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHORT_FORM_NOT_FOUND));

        Media media = shortForm.getMedia();
        Long memberId = (Long) authentication.getPrincipal();
        boolean isEditor = authentication.getAuthorities().stream()
                .anyMatch(authority -> Role.EDITOR.getKey().equals(authority.getAuthority()));
        if (isEditor && !media.getUploader().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        uploadHelper.validateOriginObjectKey(
                objectKey,
                shortForm.getOriginUrl(),
                ErrorCode.SHORTFORM_ORIGIN_OBJECT_KEY_MISMATCH
        );

        return uploadHelper.getMultipartPartCount(shortForm.getVideoSize());
    }

    @Transactional(readOnly = true)
    public PageResponse<MultipartUploadPartUrlResponse> getShortFormOriginUploadPartUrls(
            Long shortFormId, String objectKey, String uploadId,
            Integer page, Integer size, Authentication authentication) {
        ShortForm shortForm = shortFormRepository.findWithMediaAndUploaderByShortFormId(shortFormId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHORT_FORM_NOT_FOUND));

        Media media = shortForm.getMedia();
        Long memberId = (Long) authentication.getPrincipal();
        boolean isEditor = authentication.getAuthorities().stream()
                .anyMatch(authority -> Role.EDITOR.getKey().equals(authority.getAuthority()));
        if (isEditor && !media.getUploader().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        uploadHelper.validateOriginObjectKey(
                objectKey,
                shortForm.getOriginUrl(),
                ErrorCode.SHORTFORM_ORIGIN_OBJECT_KEY_MISMATCH
        );

        int totalPartCount = uploadHelper.getMultipartPartCount(shortForm.getVideoSize());
        PageResponse<UploadHelper.MultipartUploadPartUrl> partUrlPage = uploadHelper.getMultipartPartUrls(
                objectKey, uploadId, totalPartCount, page, size
        );

        List<MultipartUploadPartUrlResponse> dataList = partUrlPage.getDataList().stream()
                .map(part -> new MultipartUploadPartUrlResponse(part.partNumber(), part.uploadUrl()))
                .toList();

        return PageResponse.toPageResponse(partUrlPage.getPageInfo(), dataList);
    }
}
