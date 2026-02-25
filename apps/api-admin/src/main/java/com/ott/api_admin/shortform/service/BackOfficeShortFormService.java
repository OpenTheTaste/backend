package com.ott.api_admin.shortform.service;

import com.ott.api_admin.shortform.dto.OriginMediaTitleListResponse;
import com.ott.api_admin.shortform.dto.ShortFormDetailResponse;
import com.ott.api_admin.shortform.dto.ShortFormListResponse;
import com.ott.api_admin.shortform.mapper.BackOfficeShortFormMapper;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.media_tag.domain.MediaTag;
import com.ott.domain.media_tag.repository.MediaTagRepository;
import com.ott.domain.member.domain.Role;
import com.ott.domain.common.MediaType;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.contents.domain.Contents;
import com.ott.domain.contents.repository.ContentsRepository;
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

        @Transactional(readOnly = true)
        public PageResponse<ShortFormListResponse> getShortFormList(
                        Integer page, Integer size, String searchWord, PublicStatus publicStatus,
                        Authentication authentication) {
                Pageable pageable = PageRequest.of(page, size);

                // 1. 관리자/에디터 여부 확인
                Long memberId = (Long) authentication.getPrincipal();
                boolean isEditor = authentication.getAuthorities().stream()
                                .anyMatch(authority -> Role.EDITOR.getKey().equals(authority.getAuthority()));
                Long uploaderId = null;

                // 2. 에디터인 경우 본인이 업로드한 숏폼만 조회 가능
                if (isEditor)
                        uploaderId = memberId;

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

                // 1. Media 페이징 조회 (Series + 단편 Contents / 에피소드 제외)
                Page<Media> mediaPage = mediaRepository.findOriginMediaListBySearchWord(pageable, searchWord);

                List<Media> mediaList = mediaPage.getContent();

                // 2. mediaId를 타입별로 분리
                List<Long> seriesMediaIdList = mediaList.stream()
                                .filter(m -> m.getMediaType() == MediaType.SERIES)
                                .map(Media::getId)
                                .toList();

                List<Long> contentsMediaIdList = mediaList.stream()
                                .filter(m -> m.getMediaType() == MediaType.CONTENTS)
                                .map(Media::getId)
                                .toList();

                // 3. 일괄 조회: mediaId → entityId 매핑
                Map<Long, Long> seriesIdByMediaId = seriesRepository.findAllByMediaIdIn(seriesMediaIdList).stream()
                                .collect(Collectors.toMap(s -> s.getMedia().getId(), Series::getId));

                Map<Long, Long> contentsIdByMediaId = contentsRepository.findAllByMediaIdIn(contentsMediaIdList)
                                .stream()
                                .collect(Collectors.toMap(c -> c.getMedia().getId(), Contents::getId));

                // 4. 응답 매핑
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
                // 1. ShortForm + Media + Uploader + ShortForm.series or ShortForm.contents 한 번에
                // 조회
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
                Optional<Media> originMedia = shortForm.findOriginMedia();
                String originMediaTitle = null;
                if (originMedia.isPresent())
                        originMediaTitle = originMedia.get().getTitle();

                // 3. 태그 조회
                List<MediaTag> mediaTagList = mediaTagRepository.findWithTagAndCategoryByMediaId(mediaId); // 숏폼은 원본
                                                                                                           // 콘텐츠의 태그를
                                                                                                           // 따라가지만, 자체
                                                                                                           // 태그로 생성되어
                                                                                                           // 있음을 상정

                return backOfficeShortFormMapper.toShortFormDetailResponse(shortForm, media, uploaderNickname,
                                originMediaTitle, mediaTagList);
        }
}
