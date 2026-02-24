package com.ott.api_admin.series.service;

import com.ott.api_admin.series.dto.request.SeriesUploadRequest;
import com.ott.api_admin.series.dto.response.SeriesDetailResponse;
import com.ott.api_admin.series.dto.response.SeriesListResponse;
import com.ott.api_admin.series.dto.response.SeriesTitleListResponse;
import com.ott.api_admin.series.dto.response.SeriesUploadResponse;
import com.ott.api_admin.series.mapper.BackOfficeSeriesMapper;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.common.MediaType;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.media_tag.domain.MediaTag;
import com.ott.domain.media_tag.repository.MediaTagRepository;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;
import com.ott.domain.series.domain.Series;
import com.ott.domain.series.repository.SeriesRepository;
import com.ott.infra.s3.service.S3PresignService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BackOfficeSeriesService {

    private final BackOfficeSeriesMapper backOfficeSeriesMapper;

    private final MediaRepository mediaRepository;
    private final MediaTagRepository mediaTagRepository;
    private final SeriesRepository seriesRepository;
    private final MemberRepository memberRepository;
    private final S3PresignService s3PresignService;

    @Transactional(readOnly = true)
    public PageResponse<SeriesListResponse> getSeries(int page, int size, String searchWord) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Media> mediaPage = mediaRepository.findMediaListByMediaTypeAndSearchWord(pageable, MediaType.SERIES, searchWord);

        List<Long> mediaIdList = mediaPage.getContent().stream()
                .map(Media::getId)
                .toList();

        Map<Long, List<MediaTag>> tagListByMediaId = mediaIdList.isEmpty()
                ? Collections.emptyMap()
                : mediaTagRepository.findWithTagAndCategoryByMediaIds(mediaIdList).stream()
                .collect(Collectors.groupingBy(mt -> mt.getMedia().getId()));

        List<SeriesListResponse> responseList = mediaPage.getContent().stream()
                .map(media -> backOfficeSeriesMapper.toSeriesListResponse(
                        media,
                        tagListByMediaId.getOrDefault(media.getId(), List.of())
                ))
                .toList();

        PageInfo pageInfo = PageInfo.toPageInfo(
                mediaPage.getNumber(),
                mediaPage.getTotalPages(),
                mediaPage.getSize()
        );
        return PageResponse.toPageResponse(pageInfo, responseList);
    }

    @Transactional(readOnly = true)
    public PageResponse<SeriesTitleListResponse> getSeriesTitle(Integer page, Integer size, String searchWord) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Series> seriesPage = seriesRepository.findSeriesListWithMediaBySearchWord(pageable, searchWord);

        List<SeriesTitleListResponse> responseList = seriesPage.getContent().stream()
                .map(backOfficeSeriesMapper::toSeriesTitleList)
                .toList();

        PageInfo pageInfo = PageInfo.toPageInfo(
                seriesPage.getNumber(),
                seriesPage.getTotalPages(),
                seriesPage.getSize()
        );
        return PageResponse.toPageResponse(pageInfo, responseList);
    }

    @Transactional(readOnly = true)
    public SeriesDetailResponse getSeriesDetail(Long mediaId) {
        Series series = seriesRepository.findWithMediaAndUploaderByMediaId(mediaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERIES_NOT_FOUND));

        Media media = series.getMedia();
        String uploaderNickname = media.getUploader().getNickname();

        List<MediaTag> mediaTagList = mediaTagRepository.findWithTagAndCategoryByMediaId(mediaId);

        return backOfficeSeriesMapper.toSeriesDetailResponse(series, media, uploaderNickname, mediaTagList);
    }

    @Transactional
    public SeriesUploadResponse createSeriesUpload(SeriesUploadRequest request) {
        Member uploader = resolveUploader();
        String sanitizedPosterFileName = sanitizeFileName(request.posterFileName());
        String sanitizedThumbnailFileName = sanitizeFileName(request.thumbnailFileName());

        Media media = mediaRepository.save(
                Media.builder()
                        .uploader(uploader)
                        .title(request.title())
                        .description(request.description())
                        .posterUrl("PENDING")
                        .thumbnailUrl("PENDING")
                        .bookmarkCount(0L)
                        .likesCount(0L)
                        .mediaType(MediaType.SERIES)
                        .publicStatus(request.publicStatus())
                        .build()
        );

        Series series = seriesRepository.save(
                Series.builder()
                        .media(media)
                        .actors(request.actors())
                        .build()
        );

        Long seriesId = series.getId();
        String posterObjectKey = buildObjectKey("series", seriesId, "poster", sanitizedPosterFileName);
        String thumbnailObjectKey = buildObjectKey("series", seriesId, "thumbnail", sanitizedThumbnailFileName);
        media.updateImageKeys(
                s3PresignService.toObjectUrl(posterObjectKey),
                s3PresignService.toObjectUrl(thumbnailObjectKey)
        );

        return backOfficeSeriesMapper.toSeriesUploadResponse(
                seriesId,
                posterObjectKey,
                thumbnailObjectKey,
                s3PresignService.createPutPresignedUrl(posterObjectKey, resolveContentType(sanitizedPosterFileName)),
                s3PresignService.createPutPresignedUrl(thumbnailObjectKey, resolveContentType(sanitizedThumbnailFileName))
        );
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

    private String sanitizeFileName(String fileName) {
        String trimmed = fileName == null ? "" : fileName.trim();
        int lastDot = trimmed.lastIndexOf('.');
        String namePart = lastDot > 0 ? trimmed.substring(0, lastDot) : trimmed;
        String extPart = lastDot > 0 ? trimmed.substring(lastDot + 1) : "";

        String sanitizedName = namePart
                .replace("/", "")
                .replace("\\", "")
                .replaceAll("[^0-9A-Za-z가-힣_-]", "");
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

