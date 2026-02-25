package com.ott.api_admin.content.service;

import com.ott.api_admin.content.dto.request.ContentsUploadRequest;
import com.ott.api_admin.content.dto.response.ContentsDetailResponse;
import com.ott.api_admin.content.dto.response.ContentsListResponse;
import com.ott.api_admin.content.dto.response.ContentsUploadResponse;
import com.ott.api_admin.content.mapper.BackOfficeContentsMapper;
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

import java.util.List;

@RequiredArgsConstructor
@Service
public class BackOfficeContentsService {

    private final BackOfficeContentsMapper backOfficeContentsMapper;

    private final MediaRepository mediaRepository;
    private final MediaTagRepository mediaTagRepository;
    private final ContentsRepository contentsRepository;
    private final SeriesRepository seriesRepository;
    private final S3PresignService s3PresignService;
    private final UploadHelper uploadHelper;

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

    @Transactional
    // 콘텐츠/미디어 레코드를 생성하고 S3 업로드용 Presigned URL을 발급합니다.
    public ContentsUploadResponse createContentsUpload(ContentsUploadRequest request) {
        Member uploader = uploadHelper.resolveUploader();
        Series series = resolveSeries(request.seriesId());

        // S3 object key 안정성을 위해 파일명을 정규화합니다.
        String sanitizedPosterFileName = uploadHelper.sanitizeFileName(request.posterFileName());
        String sanitizedThumbnailFileName = uploadHelper.sanitizeFileName(request.thumbnailFileName());
        String sanitizedOriginFileName = uploadHelper.sanitizeFileName(request.originFileName());

        Media media = mediaRepository.save(
                Media.builder()
                        .uploader(uploader)
                        .title(request.title())
                        .description(request.description())
                        // 콘텐츠 ID 생성 전이라 최종 URL을 만들 수 없어 임시값을 저장합니다.
                        .posterUrl("PENDING")
                        // 콘텐츠 ID 기반 object key 확정 후 실제 S3 URL로 즉시 갱신합니다.
                        .thumbnailUrl("PENDING")
                        .bookmarkCount(0L)
                        .likesCount(0L)
                        .mediaType(MediaType.CONTENTS)
                        .publicStatus(request.publicStatus())
                        .build()
        );

        Contents contents = contentsRepository.save(
                Contents.builder()
                        .media(media)
                        .series(series)
                        .actors(request.actors())
                        .duration(request.duration())
                        .videoSize(request.videoSize())
                        // 콘텐츠 ID 생성 전이라 원본 URL을 확정할 수 없어 임시값을 저장합니다.
                        .originUrl("PENDING")
                        // 트랜스코딩 결과 URL도 ID 기반 경로 계산 후 갱신합니다.
                        .masterPlaylistUrl("PENDING")
                        .build()
        );

        Long contentsId = contents.getId();
        String posterObjectKey = uploadHelper.buildObjectKey("contents", contentsId, "poster", sanitizedPosterFileName);
        String thumbnailObjectKey = uploadHelper.buildObjectKey("contents", contentsId, "thumbnail", sanitizedThumbnailFileName);
        String originObjectKey = uploadHelper.buildObjectKey("contents", contentsId, "origin", sanitizedOriginFileName);
        String masterPlaylistObjectKey = "contents/" + contentsId + "/transcoded/master.m3u8";

        media.updateImageKeys(
                s3PresignService.toObjectUrl(posterObjectKey),
                s3PresignService.toObjectUrl(thumbnailObjectKey)
        );
        contents.updateStorageKeys(
                s3PresignService.toObjectUrl(originObjectKey),
                s3PresignService.toObjectUrl(masterPlaylistObjectKey)
        );

        return backOfficeContentsMapper.toContentsUploadResponse(
                contentsId,
                posterObjectKey,
                thumbnailObjectKey,
                originObjectKey,
                masterPlaylistObjectKey,
                s3PresignService.createPutPresignedUrl(posterObjectKey, uploadHelper.resolveImageContentType(sanitizedPosterFileName)),
                s3PresignService.createPutPresignedUrl(thumbnailObjectKey, uploadHelper.resolveImageContentType(sanitizedThumbnailFileName)),
                s3PresignService.createPutPresignedUrl(originObjectKey, uploadHelper.resolveVideoContentType(sanitizedOriginFileName))
        );
    }

    private Series resolveSeries(Long seriesId) {
        if (seriesId == null) {
            return null;
        }
        // 요청으로 전달된 seriesId의 존재 여부를 확인합니다.
        return seriesRepository.findById(seriesId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERIES_NOT_FOUND));
    }
}
