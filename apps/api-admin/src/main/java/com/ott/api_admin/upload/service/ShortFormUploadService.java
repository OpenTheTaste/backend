package com.ott.api_admin.upload.service;

import com.ott.api_admin.upload.dto.request.ShortFormUploadInitRequest;
import com.ott.api_admin.upload.dto.response.ShortFormUploadInitResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.common.MediaType;
import com.ott.domain.contents.domain.Contents;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;
import com.ott.domain.series.domain.Series;
import com.ott.domain.series.repository.SeriesRepository;
import com.ott.domain.short_form.domain.ShortForm;
import com.ott.domain.short_form.repository.ShortFormRepository;
import com.ott.infra.s3.service.S3PresignService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 숏폼 업로드 초기화 비즈니스 로직을 담당합니다.
 */
@Service
@RequiredArgsConstructor
public class ShortFormUploadService {

    private final ShortFormRepository shortFormRepository;
    private final SeriesRepository seriesRepository;
    private final ContentsRepository contentsRepository;
    private final MediaRepository mediaRepository;
    private final MemberRepository memberRepository;
    private final S3PresignService s3PresignService;

    @Transactional
    // 숏폼/미디어 레코드를 생성하고 포스터/썸네일/원본 업로드 URL을 발급합니다.
    public ShortFormUploadInitResponse createShortFormUpload(ShortFormUploadInitRequest request) {
        // 숏폼은 시리즈 또는 콘텐츠 중 하나에만 연결되도록 강제합니다.
        validateExclusiveTarget(request.seriesId(), request.contentsId());

        Member uploader = resolveUploader();
        Series series = resolveSeries(request.seriesId());
        Contents contents = resolveContents(request.contentsId());
        // S3 object key 안정성을 위해 파일명을 정규화합니다.
        String sanitizedPosterFileName = sanitizeFileName(request.posterFileName());
        String sanitizedThumbnailFileName = sanitizeFileName(request.thumbnailFileName());
        String sanitizedOriginFileName = sanitizeFileName(request.originFileName());

        Media media = mediaRepository.save(
                Media.builder()
                        .uploader(uploader)
                        .title(request.title())
                        .description(request.description())
                        // 숏폼 ID가 생성되기 전이라 최종 S3 URL을 만들 수 없어 임시값으로 저장합니다.
                        .posterUrl("PENDING")
                        // 숏폼 ID 기반 object key를 만든 뒤 실제 S3 URL로 즉시 갱신됩니다.
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
                        // 숏폼 ID 생성 후 origin object key가 확정되므로 우선 임시값으로 저장합니다.
                        .originUrl("PENDING")
                        // 트랜스코딩 결과 경로를 ID 기준으로 계산한 뒤 실제 S3 URL로 갱신됩니다.
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

        return new ShortFormUploadInitResponse(
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
        // 둘 다 비었거나 둘 다 값이 있으면 잘못된 요청입니다.
        if ((seriesId == null && contentsId == null) || (seriesId != null && contentsId != null)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private Series resolveSeries(Long seriesId) {
        if (seriesId == null) {
            return null;
        }
        // 요청으로 전달된 seriesId의 존재 여부를 확인합니다.
        return seriesRepository.findById(seriesId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERIES_NOT_FOUND));
    }

    private Contents resolveContents(Long contentsId) {
        if (contentsId == null) {
            return null;
        }
        // 요청으로 전달된 contentsId의 존재 여부를 확인합니다.
        return contentsRepository.findById(contentsId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));
    }

    private String buildObjectKey(String root, Long id, String mediaType, String fileName) {
        // S3 저장 경로 규칙: {root}/{id}/{mediaType}/{fileName}
        return root + "/" + id + "/" + mediaType + "/" + fileName;
    }

    private String resolveContentType(String fileName) {
        // 파일 확장자를 기반으로 업로드 Content-Type을 추론합니다.
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
        // 미지원 확장자는 Presigned URL 발급 전에 차단합니다.
        throw new BusinessException(ErrorCode.INVALID_INPUT);
    }

    private String sanitizeFileName(String fileName) {
        // path traversal/특수문자 이슈를 줄이기 위해 파일명을 안전한 문자 집합으로 제한합니다.
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
            // 확장자가 없으면 MIME 추론이 불가능하므로 요청을 거부합니다.
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return sanitizedName + "." + sanitizedExt;
    }

    private Member resolveUploader() {
        // JWT 필터가 설정한 인증 정보에서 현재 로그인 사용자 ID를 꺼냅니다.
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

        // 최종적으로 DB에서 업로더 회원 엔티티를 조회합니다.
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }
}
