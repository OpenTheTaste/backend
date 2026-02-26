package com.ott.api_admin.upload.support;

import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UploadHelper {

    private final MemberRepository memberRepository;

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

    public Member resolveUploader() {
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
