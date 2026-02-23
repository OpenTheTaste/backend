package com.ott.api_admin.upload.service;

import com.ott.api_admin.upload.dto.request.SeriesUploadInitRequest;
import com.ott.api_admin.upload.dto.response.SeriesUploadInitResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.common.MediaType;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;
import com.ott.domain.series.domain.Series;
import com.ott.domain.series.repository.SeriesRepository;
import com.ott.infra.s3.service.S3PresignService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 시리즈 업로드 초기화 비즈니스 로직을 담당합니다.
 */
@Service
@RequiredArgsConstructor
public class SeriesUploadService {

    private final SeriesRepository seriesRepository;
    private final MediaRepository mediaRepository;
    private final MemberRepository memberRepository;
    private final S3PresignService s3PresignService;

    @Transactional
    // 시리즈/미디어 레코드를 생성하고 포스터/썸네일 업로드 URL을 발급합니다.
    public SeriesUploadInitResponse createSeriesUpload(SeriesUploadInitRequest request) {
        Member uploader = resolveUploader();
        // S3 object key 안정성을 위해 파일명을 정규화합니다.
        String sanitizedPosterFileName = sanitizeFileName(request.posterFileName());
        String sanitizedThumbnailFileName = sanitizeFileName(request.thumbnailFileName());

        Media media = mediaRepository.save(
                Media.builder()
                        .uploader(uploader)
                        .title(request.title())
                        .description(request.description())
                        // 시리즈 ID가 생성되기 전이라 최종 S3 URL을 만들 수 없어 임시값으로 저장합니다.
                        .posterUrl("PENDING")
                        // 시리즈 ID 기반 object key를 만든 뒤 실제 S3 URL로 즉시 갱신됩니다.
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

        return new SeriesUploadInitResponse(
                seriesId,
                posterObjectKey,
                thumbnailObjectKey,
                s3PresignService.createPutPresignedUrl(posterObjectKey, resolveContentType(sanitizedPosterFileName)),
                s3PresignService.createPutPresignedUrl(thumbnailObjectKey, resolveContentType(sanitizedThumbnailFileName))
        );
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
