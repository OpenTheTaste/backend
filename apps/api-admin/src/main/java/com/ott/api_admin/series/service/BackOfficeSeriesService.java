package com.ott.api_admin.series.service;

import com.ott.api_admin.series.dto.request.SeriesUploadRequest;
import com.ott.api_admin.series.dto.response.SeriesDetailResponse;
import com.ott.api_admin.series.dto.response.SeriesListResponse;
import com.ott.api_admin.series.dto.response.SeriesTitleListResponse;
import com.ott.api_admin.series.dto.response.SeriesUploadResponse;
import com.ott.api_admin.series.mapper.BackOfficeSeriesMapper;
import com.ott.api_admin.upload.support.UploadHelper;
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
import com.ott.domain.series.domain.Series;
import com.ott.domain.series.repository.SeriesRepository;
import com.ott.infra.s3.service.S3PresignService;
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
public class BackOfficeSeriesService {

    private final BackOfficeSeriesMapper backOfficeSeriesMapper;

    private final MediaRepository mediaRepository;
    private final MediaTagRepository mediaTagRepository;
    private final SeriesRepository seriesRepository;
    private final S3PresignService s3PresignService;
    private final UploadHelper uploadHelper;

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
        Member uploader = uploadHelper.resolveUploader();
        String sanitizedPosterFileName = uploadHelper.sanitizeFileName(request.posterFileName());
        String sanitizedThumbnailFileName = uploadHelper.sanitizeFileName(request.thumbnailFileName());

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
        String posterObjectKey = uploadHelper.buildObjectKey("series", seriesId, "poster", sanitizedPosterFileName);
        String thumbnailObjectKey = uploadHelper.buildObjectKey("series", seriesId, "thumbnail", sanitizedThumbnailFileName);
        media.updateImageKeys(
                s3PresignService.toObjectUrl(posterObjectKey),
                s3PresignService.toObjectUrl(thumbnailObjectKey)
        );

        return backOfficeSeriesMapper.toSeriesUploadResponse(
                seriesId,
                posterObjectKey,
                thumbnailObjectKey,
                s3PresignService.createPutPresignedUrl(posterObjectKey, uploadHelper.resolveImageContentType(sanitizedPosterFileName)),
                s3PresignService.createPutPresignedUrl(thumbnailObjectKey, uploadHelper.resolveImageContentType(sanitizedThumbnailFileName))
        );
    }
}

