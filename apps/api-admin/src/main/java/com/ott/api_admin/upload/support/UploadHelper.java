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

    public String buildObjectKey(String root, Long id, String mediaType, String fileName) {
        return root + "/" + id + "/" + mediaType + "/" + fileName;
    }

    public String resolveImageContentType(String fileName) {
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

    public String resolveVideoContentType(String fileName) {
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

    public String sanitizeFileName(String fileName) {
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
