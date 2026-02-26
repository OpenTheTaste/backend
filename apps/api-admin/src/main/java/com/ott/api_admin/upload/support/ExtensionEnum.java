package com.ott.api_admin.upload.support;

import java.util.Arrays;
import java.util.Locale;

public enum ExtensionEnum {
    JPG("jpg", "image/jpeg", Category.IMAGE),
    JPEG("jpeg", "image/jpeg", Category.IMAGE),
    PNG("png", "image/png", Category.IMAGE),
    WEBP("webp", "image/webp", Category.IMAGE),
    MP4("mp4", "video/mp4", Category.VIDEO),
    MOV("mov", "video/quicktime", Category.VIDEO),
    WEBM("webm", "video/webm", Category.VIDEO),
    M4V("m4v", "video/x-m4v", Category.VIDEO);

    private final String extension;
    private final String contentType;
    private final Category category;

    ExtensionEnum(String extension, String contentType, Category category) {
        this.extension = extension;
        this.contentType = contentType;
        this.category = category;
    }

    public static String resolveImageContentType(String fileName) {
        return resolveContentType(fileName, Category.IMAGE);
    }

    public static String resolveVideoContentType(String fileName) {
        return resolveContentType(fileName, Category.VIDEO);
    }

    private static String resolveContentType(String fileName, Category expectedCategory) {
        String extractedExtension = extractExtension(fileName);

        return Arrays.stream(values())
                .filter(candidate -> candidate.category == expectedCategory)
                .filter(candidate -> candidate.extension.equals(extractedExtension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported file extension: " + extractedExtension))
                .contentType;
    }

    private static String extractExtension(String fileName) {
        String trimmed = fileName.trim();
        int extensionDelimiterIndex = trimmed.lastIndexOf('.');
        if (extensionDelimiterIndex < 0 || extensionDelimiterIndex == trimmed.length() - 1) {
            throw new IllegalArgumentException("File extension is missing");
        }
        return trimmed.substring(extensionDelimiterIndex + 1).toLowerCase(Locale.ROOT);
    }

    private enum Category {
        IMAGE,
        VIDEO
    }
}
