package com.ott.api_admin.shortform.service;

import com.ott.api_admin.shortform.dto.response.OriginMediaTitleListResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormDetailResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormListResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormUploadResponse;
import com.ott.api_admin.shortform.dto.request.ShortFormUploadRequest;
import com.ott.api_admin.shortform.mapper.BackOfficeShortFormMapper;
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
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.domain.Role;
import com.ott.domain.member.repository.MemberRepository;
import com.ott.domain.series.domain.Series;
import com.ott.domain.series.repository.SeriesRepository;
import com.ott.domain.short_form.domain.ShortForm;
import com.ott.domain.short_form.repository.ShortFormRepository;
import com.ott.infra.s3.service.S3PresignService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BackOfficeShortFormService {

    private final BackOfficeShortFormMapper backOfficeShortFormMapper;

    private final MediaRepository mediaRepository;
    private final MediaTagRepository mediaTagRepository;
    private final SeriesRepository seriesRepository;
    private final ContentsRepository contentsRepository;
    private final ShortFormRepository shortFormRepository;
    private final MemberRepository memberRepository;
    private final S3PresignService s3PresignService;

    @Transactional(readOnly = true)
    public PageResponse<ShortFormListResponse> getShortFormList(
            Integer page, Integer size, String searchWord, PublicStatus publicStatus, Authentication authentication
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Long memberId = (Long) authentication.getPrincipal();
        boolean isEditor = authentication.getAuthorities().stream()
                .anyMatch(authority -> Role.EDITOR.getKey().equals(authority.getAuthority()));
        Long uploaderId = null;

        if (isEditor) {
            uploaderId = memberId;
        }

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

        Map<Long, Long> contentsIdByMediaId = contentsRepository.findAllByMediaIdIn(contentsMediaIdList).stream()
                .collect(Collectors.toMap(c -> c.getMedia().getId(), Contents::getId));

        List<OriginMediaTitleListResponse> responseList = mediaList.stream()
                .map(m -> backOfficeShortFormMapper.toOriginMediaTitleListResponse(m, seriesIdByMediaId, contentsIdByMediaId))
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
        ShortForm shortForm = shortFormRepository.findWithMediaAndUploaderByMediaId(mediaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));

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
        if (originMedia.isPresent()) {
            originMediaTitle = originMedia.get().getTitle();
        }

        List<MediaTag> mediaTagList = mediaTagRepository.findWithTagAndCategoryByMediaId(mediaId);

        return backOfficeShortFormMapper.toShortFormDetailResponse(shortForm, media, uploaderNickname, originMediaTitle, mediaTagList);
    }

    @Transactional
    public ShortFormUploadResponse createShortFormUpload(ShortFormUploadRequest request) {
        validateExclusiveTarget(request.seriesId(), request.contentsId());

        Member uploader = resolveUploader();
        Series series = resolveSeries(request.seriesId());
        Contents contents = resolveContents(request.contentsId());
        String sanitizedPosterFileName = sanitizeFileName(request.posterFileName());
        String sanitizedThumbnailFileName = sanitizeFileName(request.thumbnailFileName());
        String sanitizedOriginFileName = sanitizeFileName(request.originFileName());

        Media media = mediaRepository.save(
                Media.builder()
                        .uploader(uploader)
                        .title(request.title())
                        .description(request.description())
                        .posterUrl("PENDING")
                        .thumbnailUrl("PENDING")
                        .bookmarkCount(0L)
                        .likesCount(0L)
                        .mediaType(MediaType.SHORT_FORM)
                        .publicStatus(request.publicStatus())
                        .build()
        );

        ShortForm shortForm = shortFormRepository.save(
                ShortForm.builder()
                        .media(media)
                        .series(series)
                        .contents(contents)
                        .duration(request.duration())
                        .videoSize(request.videoSize())
                        .originUrl("PENDING")
                        .masterPlaylistUrl("PENDING")
                        .build()
        );

        Long shortFormId = shortForm.getId();
        String posterObjectKey = buildObjectKey("short-forms", shortFormId, "poster", sanitizedPosterFileName);
        String thumbnailObjectKey = buildObjectKey("short-forms", shortFormId, "thumbnail", sanitizedThumbnailFileName);
        String originObjectKey = buildObjectKey("short-forms", shortFormId, "origin", sanitizedOriginFileName);
        String masterPlaylistObjectKey = "short-forms/" + shortFormId + "/transcoded/master.m3u8";

        media.updateImageKeys(
                s3PresignService.toObjectUrl(posterObjectKey),
                s3PresignService.toObjectUrl(thumbnailObjectKey)
        );
        shortForm.updateStorageKeys(
                s3PresignService.toObjectUrl(originObjectKey),
                s3PresignService.toObjectUrl(masterPlaylistObjectKey)
        );

        return backOfficeShortFormMapper.toShortFormUploadResponse(
                shortFormId,
                posterObjectKey,
                thumbnailObjectKey,
                originObjectKey,
                masterPlaylistObjectKey,
                s3PresignService.createPutPresignedUrl(posterObjectKey, resolveContentType(sanitizedPosterFileName)),
                s3PresignService.createPutPresignedUrl(thumbnailObjectKey, resolveContentType(sanitizedThumbnailFileName)),
                s3PresignService.createPutPresignedUrl(originObjectKey, resolveOriginContentType(sanitizedOriginFileName))
        );
    }

    private void validateExclusiveTarget(Long seriesId, Long contentsId) {
        if ((seriesId == null && contentsId == null) || (seriesId != null && contentsId != null)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private Series resolveSeries(Long seriesId) {
        if (seriesId == null) {
            return null;
        }
        return seriesRepository.findById(seriesId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERIES_NOT_FOUND));
    }

    private Contents resolveContents(Long contentsId) {
        if (contentsId == null) {
            return null;
        }
        return contentsRepository.findById(contentsId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));
    }

    private String buildObjectKey(String root, Long id, String mediaType, String fileName) {
        return root + "/" + id + "/" + mediaType + "/" + fileName;
    }

    private String resolveContentType(String fileName) {
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lowerFileName.endsWith(".png")) {
            return "image/png";
        }
        if (lowerFileName.endsWith(".webp")) {
            return "image/webp";
        }
        throw new BusinessException(ErrorCode.INVALID_INPUT);
    }

    private String resolveOriginContentType(String fileName) {
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".mp4")) {
            return "video/mp4";
        }
        if (lowerFileName.endsWith(".mov")) {
            return "video/quicktime";
        }
        if (lowerFileName.endsWith(".webm")) {
            return "video/webm";
        }
        if (lowerFileName.endsWith(".m4v")) {
            return "video/x-m4v";
        }
        throw new BusinessException(ErrorCode.INVALID_INPUT);
    }

    private String sanitizeFileName(String fileName) {
        String trimmed = fileName == null ? "" : fileName.trim();
        int lastDot = trimmed.lastIndexOf('.');
        String namePart = lastDot > 0 ? trimmed.substring(0, lastDot) : trimmed;
        String extPart = lastDot > 0 ? trimmed.substring(lastDot + 1) : "";

        String sanitizedName = namePart
                .replace("/", "")
                .replace("\\", "")
                .replaceAll("[^0-9A-Za-z_-]", "");
        String sanitizedExt = extPart.replaceAll("[^0-9A-Za-z]", "").toLowerCase();

        if (sanitizedName.isBlank()) {
            sanitizedName = "file";
        }
        if (sanitizedExt.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return sanitizedName + "." + sanitizedExt;
    }

    private Member resolveUploader() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long memberId;
        try {
            memberId = Long.valueOf(String.valueOf(principal));
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }
}

