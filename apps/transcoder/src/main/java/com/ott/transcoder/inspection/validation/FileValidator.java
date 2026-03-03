package com.ott.transcoder.inspection.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ott.transcoder.exception.TranscodeErrorCode;
import com.ott.transcoder.exception.fatal.InvalidInputException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.Map;

/**
 * probe 전 파일 수준 검증.
 *
 * ffprobe를 실행하기 전에, 파일 자체가 유효한 미디어 파일인지 기본 방어선을 친다.
 * 여기서 걸러지면 ffprobe를 돌릴 필요조차 없다.
 *
 * 검증 항목:
 *   1. 파일 존재 여부
 *   2. 파일 크기 (0 bytes / 상한 초과)
 *   3. 읽기 권한
 *   4. 매직 바이트 — 실제 미디어 포맷인지 확인
 *   5. 확장자 vs 매직 바이트 불일치 감지
 */
@Slf4j
@Component
public class FileValidator {

    /** 파일 크기 상한 (기본 10GB) */
    @Value("${transcoder.validation.max-file-size-bytes:10737418240}")
    private long maxFileSizeBytes;

    private static final Map<String, String> EXTENSION_TO_FORMAT = Map.of(
            "mp4", "MP4",
            "mov", "MOV",
            "mkv", "MKV",
            "webm", "WEBM",
            "avi", "AVI",
            "flv", "FLV",
            "ts", "MPEG-TS"
    );

    public void validate(Path inputFile) {
        // 1. 파일 존재
        if (!Files.exists(inputFile)) {
            throw new InvalidInputException(TranscodeErrorCode.FILE_NOT_FOUND,
                    "파일이 존재하지 않음 - " + inputFile);
        }

        // 2. 읽기 권한
        if (!Files.isReadable(inputFile)) {
            throw new InvalidInputException(TranscodeErrorCode.FILE_NOT_READABLE,
                    "파일 읽기 권한 없음 - " + inputFile);
        }

        // 3. 파일 크기
        long fileSize;
        try {
            fileSize = Files.size(inputFile);
        } catch (IOException e) {
            throw new InvalidInputException(TranscodeErrorCode.FILE_NOT_READABLE,
                    "파일 크기 확인 실패 - " + inputFile, e);
        }

        if (fileSize == 0) {
            throw new InvalidInputException(TranscodeErrorCode.FILE_EMPTY,
                    "파일 크기가 0 bytes - " + inputFile);
        }
        if (fileSize > maxFileSizeBytes) {
            throw new InvalidInputException(TranscodeErrorCode.FILE_SIZE_EXCEEDED,
                    "파일 크기 상한 초과 - size: " + fileSize + " bytes, max: " + maxFileSizeBytes + " bytes");
        }

        // 4. 매직 바이트 검증
        String detectedFormat = detectFormatByMagicBytes(inputFile);
        if (detectedFormat == null) {
            throw new InvalidInputException(TranscodeErrorCode.INVALID_FILE_FORMAT,
                    "알 수 없는 파일 포맷 (매직 바이트 불일치) - " + inputFile);
        }

        // 5. 확장자 vs 매직 바이트 불일치 경고
        String extension = getExtension(inputFile);
        String expectedFormat = EXTENSION_TO_FORMAT.get(extension);
        if (expectedFormat != null && !expectedFormat.equals(detectedFormat)) {
            // MOV와 MP4는 동일한 ftyp 계열이므로 호환으로 취급
            if (!isCompatibleFormat(expectedFormat, detectedFormat)) {
                log.warn("확장자-포맷 불일치 - file: {}, extension: .{} ({}), detected: {}",
                        inputFile.getFileName(), extension, expectedFormat, detectedFormat);
            }
        }

        log.info("파일 검증 통과 - file: {}, size: {} bytes, format: {}",
                inputFile.getFileName(), fileSize, detectedFormat);
    }

    private String detectFormatByMagicBytes(Path inputFile) {
        byte[] header = new byte[12];
        int bytesRead;

        try (InputStream is = Files.newInputStream(inputFile)) {
            bytesRead = is.read(header);
        } catch (IOException e) {
            throw new InvalidInputException(TranscodeErrorCode.FILE_NOT_READABLE,
                    "매직 바이트 읽기 실패 - " + inputFile, e);
        }

        if (bytesRead < 8) {
            return null;
        }

        // MP4/MOV: offset 4~7이 "ftyp"
        if (header[4] == 0x66 && header[5] == 0x74 && header[6] == 0x79 && header[7] == 0x70) {
            return "MP4"; // MP4/MOV/3GP 계열
        }

        // MKV/WebM: EBML 헤더 (0x1A 0x45 0xDF 0xA3)
        if (header[0] == 0x1A && header[1] == 0x45 && header[2] == (byte) 0xDF && header[3] == (byte) 0xA3) {
            return "MKV"; // MKV/WebM
        }

        // AVI: "RIFF"
        if (header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F') {
            return "AVI";
        }

        // FLV: "FLV"
        if (header[0] == 'F' && header[1] == 'L' && header[2] == 'V') {
            return "FLV";
        }

        // MPEG-TS: sync byte 0x47
        if (header[0] == 0x47) {
            return "MPEG-TS";
        }

        log.debug("매직 바이트 미식별 - hex: {}", HexFormat.of().formatHex(header, 0, bytesRead));
        return null;
    }

    private String getExtension(Path file) {
        String fileName = file.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) return "";
        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    /** MP4/MOV는 동일 ftyp 계열이므로 호환으로 취급 */
    private boolean isCompatibleFormat(String expected, String detected) {
        if ("MP4".equals(expected) && "MP4".equals(detected)) return true;
        if ("MOV".equals(expected) && "MP4".equals(detected)) return true;
        if ("WEBM".equals(expected) && "MKV".equals(detected)) return true;
        return false;
    }
}
