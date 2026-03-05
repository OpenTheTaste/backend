package com.ott.api_admin.upload.support;

import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;
import com.ott.infra.s3.service.S3PresignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Component
public class UploadHelper {

    private final MemberRepository memberRepository;
    private final S3PresignService s3PresignService;

    public String buildObjectKey(String resourceRoot, Long resourceId, String assetType, String fileName) {
        return resourceRoot + "/" + resourceId + "/" + assetType + "/" + fileName;
    }

    public String resolveImageContentType(String fileName) {
        try {
            return ExtensionEnum.resolveImageContentType(fileName);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_IMAGE_EXTENSION);
        }
    }

    public String resolveVideoContentType(String fileName) {
        try {
            return ExtensionEnum.resolveVideoContentType(fileName);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_VIDEO_EXTENSION);
        }
    }

    public String sanitizeFileName(String fileName) {
        String trimmed = fileName == null ? "" : fileName.trim();
        int extensionDelimiterIndex = trimmed.lastIndexOf('.');
        String baseName = extensionDelimiterIndex > 0 ? trimmed.substring(0, extensionDelimiterIndex) : trimmed;
        String extensionPart = extensionDelimiterIndex > 0 ? trimmed.substring(extensionDelimiterIndex + 1) : "";

        String sanitizedBaseName = baseName
                .replace("/", "")
                .replace("\\", "")
                .replaceAll("[^0-9A-Za-z가-힣_-]", "");
        String sanitizedExtension = extensionPart.replaceAll("[^0-9A-Za-z]", "").toLowerCase();

        if (sanitizedBaseName.isBlank()) {
            sanitizedBaseName = "file";
        }
        if (sanitizedExtension.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_EXTENSION);
        }
        return sanitizedBaseName + "." + sanitizedExtension;
    }

    public Member resolveUploader(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    public UploadFileResult prepareRequiredUpload(
            String resourceRoot,
            Long resourceId,
            String assetType,
            String fileName,
            boolean isVideo
    ) {
        String sanitizedFileName = sanitizeFileName(fileName);
        String objectKey = buildObjectKey(resourceRoot, resourceId, assetType, sanitizedFileName);
        String contentType = isVideo
                ? resolveVideoContentType(sanitizedFileName)
                : resolveImageContentType(sanitizedFileName);
        String objectUrl = s3PresignService.toObjectUrl(objectKey);
        String uploadUrl = s3PresignService.createPutPresignedUrl(objectKey, contentType);
        return new UploadFileResult(objectKey, objectUrl, uploadUrl);
    }

    public UploadFileResult prepareOptionalUpload(
            String resourceRoot,
            Long resourceId,
            String assetType,
            String fileName,
            boolean isVideo
    ) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }
        return prepareRequiredUpload(resourceRoot, resourceId, assetType, fileName, isVideo);
    }

    public String buildMasterPlaylistObjectKey(String resourceRoot, Long resourceId) {
        return resourceRoot + "/" + resourceId + "/transcoded/master.m3u8";
    }

    public String toObjectUrl(String objectKey) {
        return s3PresignService.toObjectUrl(objectKey);
    }

    public ImageCreateUploadResult prepareImageCreate(
            String resourceRoot,
            Long resourceId,
            String posterFileName,
            String thumbnailFileName
    ) {
        UploadFileResult posterUpload = prepareRequiredUpload(resourceRoot, resourceId, "poster", posterFileName, false);
        UploadFileResult thumbnailUpload = prepareRequiredUpload(resourceRoot, resourceId, "thumbnail", thumbnailFileName, false);

        return new ImageCreateUploadResult(
                posterUpload.objectKey(),
                thumbnailUpload.objectKey(),
                posterUpload.objectUrl(),
                thumbnailUpload.objectUrl(),
                posterUpload.uploadUrl(),
                thumbnailUpload.uploadUrl()
        );
    }

    public ImageUpdateUploadResult prepareImageUpdate(
            String resourceRoot,
            Long resourceId,
            String posterFileName,
            String thumbnailFileName,
            String currentPosterUrl,
            String currentThumbnailUrl
    ) {
        UploadFileResult posterUpload = prepareOptionalUpload(resourceRoot, resourceId, "poster", posterFileName, false);
        UploadFileResult thumbnailUpload = prepareOptionalUpload(resourceRoot, resourceId, "thumbnail", thumbnailFileName, false);

        String finalPosterUrl = currentPosterUrl;
        String finalThumbnailUrl = currentThumbnailUrl;
        String posterObjectKey = null;
        String thumbnailObjectKey = null;
        String posterUploadUrl = null;
        String thumbnailUploadUrl = null;

        if (posterUpload != null) {
            finalPosterUrl = posterUpload.objectUrl();
            posterObjectKey = posterUpload.objectKey();
            posterUploadUrl = posterUpload.uploadUrl();
        }
        if (thumbnailUpload != null) {
            finalThumbnailUrl = thumbnailUpload.objectUrl();
            thumbnailObjectKey = thumbnailUpload.objectKey();
            thumbnailUploadUrl = thumbnailUpload.uploadUrl();
        }

        return new ImageUpdateUploadResult(
                posterObjectKey,
                thumbnailObjectKey,
                posterUploadUrl,
                thumbnailUploadUrl,
                finalPosterUrl,
                finalThumbnailUrl
        );
    }

    public MediaCreateUploadResult prepareMediaCreate(
            String resourceRoot,
            Long resourceId,
            String posterFileName,
            String thumbnailFileName,
            String originFileName
    ) {
        UploadFileResult posterUpload = prepareRequiredUpload(resourceRoot, resourceId, "poster", posterFileName, false);
        UploadFileResult thumbnailUpload = prepareRequiredUpload(resourceRoot, resourceId, "thumbnail", thumbnailFileName, false);
        UploadFileResult originUpload = prepareRequiredUpload(resourceRoot, resourceId, "origin", originFileName, true);

        String masterPlaylistObjectKey = buildMasterPlaylistObjectKey(resourceRoot, resourceId);
        String masterPlaylistObjectUrl = toObjectUrl(masterPlaylistObjectKey);

        return new MediaCreateUploadResult(
                posterUpload.objectKey(),
                thumbnailUpload.objectKey(),
                originUpload.objectKey(),
                masterPlaylistObjectKey,
                posterUpload.objectUrl(),
                thumbnailUpload.objectUrl(),
                originUpload.objectUrl(),
                masterPlaylistObjectUrl,
                posterUpload.uploadUrl(),
                thumbnailUpload.uploadUrl(),
                originUpload.uploadUrl()
        );
    }

    public MediaUpdateUploadResult prepareMediaUpdate(
            String resourceRoot,
            Long resourceId,
            String posterFileName,
            String thumbnailFileName,
            String originFileName,
            String currentPosterUrl,
            String currentThumbnailUrl,
            String currentOriginUrl,
            String currentMasterPlaylistUrl
    ) {
        UploadFileResult posterUpload = prepareOptionalUpload(resourceRoot, resourceId, "poster", posterFileName, false);
        UploadFileResult thumbnailUpload = prepareOptionalUpload(resourceRoot, resourceId, "thumbnail", thumbnailFileName, false);
        UploadFileResult originUpload = prepareOptionalUpload(resourceRoot, resourceId, "origin", originFileName, true);

        String posterObjectKey = null;
        String thumbnailObjectKey = null;
        String originObjectKey = null;
        String posterUploadUrl = null;
        String thumbnailUploadUrl = null;
        String originUploadUrl = null;
        String finalPosterUrl = currentPosterUrl;
        String finalThumbnailUrl = currentThumbnailUrl;
        String finalOriginUrl = currentOriginUrl;
        String masterPlaylistObjectKey = buildMasterPlaylistObjectKey(resourceRoot, resourceId);
        String finalMasterPlaylistUrl = currentMasterPlaylistUrl;

        if (posterUpload != null) {
            posterObjectKey = posterUpload.objectKey();
            posterUploadUrl = posterUpload.uploadUrl();
            finalPosterUrl = posterUpload.objectUrl();
        }
        if (thumbnailUpload != null) {
            thumbnailObjectKey = thumbnailUpload.objectKey();
            thumbnailUploadUrl = thumbnailUpload.uploadUrl();
            finalThumbnailUrl = thumbnailUpload.objectUrl();
        }
        if (originUpload != null) {
            originObjectKey = originUpload.objectKey();
            originUploadUrl = originUpload.uploadUrl();
            finalOriginUrl = originUpload.objectUrl();
            finalMasterPlaylistUrl = toObjectUrl(masterPlaylistObjectKey);
        }

        return new MediaUpdateUploadResult(
                posterObjectKey,
                thumbnailObjectKey,
                originObjectKey,
                masterPlaylistObjectKey,
                posterUploadUrl,
                thumbnailUploadUrl,
                originUploadUrl,
                finalPosterUrl,
                finalThumbnailUrl,
                finalOriginUrl,
                finalMasterPlaylistUrl
        );
    }

    public record UploadFileResult(
            String objectKey,
            String objectUrl,
            String uploadUrl
    ) {
    }

    public record ImageCreateUploadResult(
            String posterObjectKey,
            String thumbnailObjectKey,
            String posterObjectUrl,
            String thumbnailObjectUrl,
            String posterUploadUrl,
            String thumbnailUploadUrl
    ) {
    }

    public record ImageUpdateUploadResult(
            String posterObjectKey,
            String thumbnailObjectKey,
            String posterUploadUrl,
            String thumbnailUploadUrl,
            String nextPosterUrl,
            String nextThumbnailUrl
    ) {
    }

    public record MediaCreateUploadResult(
            String posterObjectKey,
            String thumbnailObjectKey,
            String originObjectKey,
            String masterPlaylistObjectKey,
            String posterObjectUrl,
            String thumbnailObjectUrl,
            String originObjectUrl,
            String masterPlaylistObjectUrl,
            String posterUploadUrl,
            String thumbnailUploadUrl,
            String originUploadUrl
    ) {
    }

    public record MediaUpdateUploadResult(
            String posterObjectKey,
            String thumbnailObjectKey,
            String originObjectKey,
            String masterPlaylistObjectKey,
            String posterUploadUrl,
            String thumbnailUploadUrl,
            String originUploadUrl,
            String nextPosterUrl,
            String nextThumbnailUrl,
            String nextOriginUrl,
            String nextMasterPlaylistUrl
    ) {
    }
}
