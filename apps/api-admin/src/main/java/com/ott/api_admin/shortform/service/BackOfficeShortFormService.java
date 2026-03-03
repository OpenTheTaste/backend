package com.ott.api_admin.shortform.service;

import com.ott.api_admin.shortform.dto.request.ShortFormUploadRequest;
import com.ott.api_admin.shortform.dto.request.ShortFormUpdateRequest;
import com.ott.api_admin.shortform.dto.response.OriginMediaTitleListResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormDetailResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormListResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormUpdateResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormUploadResponse;
import com.ott.api_admin.shortform.mapper.BackOfficeShortFormMapper;
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
        public PageResponse<OriginMediaTitleListResponse> getOriginMediaTitle(Integer page, Integer size,
                        String searchWord) {
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

                return backOfficeShortFormMapper.toShortFormDetailResponse(shortForm, media, uploaderNickname,
                                originMediaTitle, mediaTagList);
        }

        @Transactional
        public ShortFormUploadResponse createShortFormUpload(ShortFormUploadRequest request, Long memberId) {
                validateExclusiveTarget(request.seriesId(), request.contentsId());

                Member uploader = uploadHelper.resolveUploader(memberId);
                Series series = resolveSeries(request.seriesId());
                Contents contents = resolveContents(request.contentsId());

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
                                                .build());

                ShortForm shortForm = shortFormRepository.save(
                                ShortForm.builder()
                                                .media(media)
                                                .series(series)
                                                .contents(contents)
                                                .duration(request.duration())
                                                .videoSize(request.videoSize())
                                                .originUrl("PENDING")
                                                .masterPlaylistUrl("PENDING")
                                                .build());

                Long shortFormId = shortForm.getId();
                UploadHelper.MediaCreateUploadResult mediaCreateUploadResult = uploadHelper.prepareMediaCreate(
                                "short-forms", shortFormId, request.posterFileName(), request.thumbnailFileName(), request.originFileName()
                );

                media.updateImageKeys(
                                mediaCreateUploadResult.posterObjectUrl(),
                                mediaCreateUploadResult.thumbnailObjectUrl());
                shortForm.updateStorageKeys(
                                mediaCreateUploadResult.originObjectUrl(),
                                mediaCreateUploadResult.masterPlaylistObjectUrl());

                Long originMediaId = resolveOriginMediaId(series, contents);
                inheritOriginMediaTags(media, originMediaId);

                return backOfficeShortFormMapper.toShortFormUploadResponse(
                                shortFormId,
                                mediaCreateUploadResult.posterObjectKey(),
                                mediaCreateUploadResult.thumbnailObjectKey(),
                                mediaCreateUploadResult.originObjectKey(),
                                mediaCreateUploadResult.masterPlaylistObjectKey(),
                                mediaCreateUploadResult.posterUploadUrl(),
                                mediaCreateUploadResult.thumbnailUploadUrl(),
                                mediaCreateUploadResult.originUploadUrl());
        }

        @Transactional
        public ShortFormUpdateResponse updateShortFormUpload(Long mediaId, ShortFormUpdateRequest request, Authentication authentication) {
                ShortForm shortForm = shortFormRepository.findWithMediaAndUploaderByMediaId(mediaId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));

                Media media = shortForm.getMedia();
                Long memberId = (Long) authentication.getPrincipal();
                boolean isEditor = authentication.getAuthorities().stream()
                                .anyMatch(authority -> Role.EDITOR.getKey().equals(authority.getAuthority()));
                if (isEditor && !media.getUploader().getId().equals(memberId)) {
                        throw new BusinessException(ErrorCode.FORBIDDEN);
                }

                validateExclusiveTarget(request.seriesId(), request.contentsId());
                Series series = resolveSeries(request.seriesId());
                Contents contents = resolveContents(request.contentsId());

                media.updateMetadata(request.title(), request.description(), request.publicStatus());
                shortForm.updateMetadata(series, contents, request.duration(), request.videoSize());

                Long shortFormId = shortForm.getId();
                UploadHelper.MediaUpdateUploadResult mediaUpdateUploadResult = uploadHelper.prepareMediaUpdate(
                                "short-forms",
                                shortFormId,
                                request.posterFileName(),
                                request.thumbnailFileName(),
                                request.originFileName(),
                                media.getPosterUrl(),
                                media.getThumbnailUrl(),
                                shortForm.getOriginUrl(),
                                shortForm.getMasterPlaylistUrl()
                );

                media.updateImageKeys(
                                mediaUpdateUploadResult.nextPosterUrl(),
                                mediaUpdateUploadResult.nextThumbnailUrl()
                );
                shortForm.updateStorageKeys(
                                mediaUpdateUploadResult.nextOriginUrl(),
                                mediaUpdateUploadResult.nextMasterPlaylistUrl()
                );

                Long originMediaId = resolveOriginMediaId(series, contents);
                mediaTagRepository.deleteAllByMedia_Id(media.getId());
                inheritOriginMediaTags(media, originMediaId);

                return backOfficeShortFormMapper.toShortFormUpdateResponse(
                                shortFormId,
                                mediaUpdateUploadResult.posterObjectKey(),
                                mediaUpdateUploadResult.thumbnailObjectKey(),
                                mediaUpdateUploadResult.originObjectKey(),
                                mediaUpdateUploadResult.masterPlaylistObjectKey(),
                                mediaUpdateUploadResult.posterUploadUrl(),
                                mediaUpdateUploadResult.thumbnailUploadUrl(),
                                mediaUpdateUploadResult.originUploadUrl());
        }

        private void validateExclusiveTarget(Long seriesId, Long contentsId) {
                if ((seriesId == null && contentsId == null) || (seriesId != null && contentsId != null)) {
                        throw new BusinessException(ErrorCode.INVALID_SHORTFORM_TARGET);
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
                Contents contents = contentsRepository.findById(contentsId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));
                // 시리즈에 속한 콘텐츠는 숏폼 원본으로 허용하지 않습니다.
                if (contents.getSeries() != null) {
                        throw new BusinessException(ErrorCode.INVALID_SHORTFORM_CONTENTS_TARGET);
                }
                return contents;
        }

        private Long resolveOriginMediaId(Series series, Contents contents) {
                if (series != null) {
                        return series.getMedia().getId();
                }
                if (contents != null) {
                        return contents.getMedia().getId();
                }
                throw new BusinessException(ErrorCode.SHORTFORM_ORIGIN_MEDIA_NOT_FOUND);
        }

        private void inheritOriginMediaTags(Media targetMedia, Long originMediaId) {
                List<MediaTag> originMediaTagList = mediaTagRepository.findWithTagAndCategoryByMediaId(originMediaId);
                if (originMediaTagList.isEmpty()) {
                        return;
                }

                List<MediaTag> targetMediaTagList = originMediaTagList.stream()
                                .map(originMediaTag -> MediaTag.builder()
                                                .media(targetMedia)
                                                .tag(originMediaTag.getTag())
                                                .build())
                                .toList();
                mediaTagRepository.saveAll(targetMediaTagList);
        }
}
