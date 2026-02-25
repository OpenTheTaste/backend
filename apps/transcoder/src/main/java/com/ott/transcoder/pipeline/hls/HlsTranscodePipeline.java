package com.ott.transcoder.pipeline.hls;

import com.ott.domain.video_profile.domain.Resolution;
import com.ott.transcoder.pipeline.CommandPipeline;
import com.ott.transcoder.storage.VideoStorage;
import com.ott.transcoder.transcode.FfmpegExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * HLS 트랜스코딩 파이프라인
 *
 * 전체 흐름을 조율:
 * 1. 임시 작업 디렉토리 생성
 * 2. 원본 다운로드 (VideoStorage)
 * 3. 해상도별 FFmpeg HLS 트랜스코딩 (360p → 720p → 1080p 순차 실행)
 * 4. 마스터 플레이리스트 생성 (master.m3u8)
 * 5. 결과물 업로드 (VideoStorage)
 * 6. 임시 작업 디렉토리 정리 (성공/실패 모두)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HlsTranscodePipeline implements CommandPipeline {

    private static final List<Resolution> TARGET_RESOLUTION_LIST = List.of(
            Resolution.P360, Resolution.P720, Resolution.P1080
    );

    private final VideoStorage videoStorage;
    private final FfmpegExecutor ffmpegExecutor;
    private final MasterPlaylistGenerator masterPlaylistGenerator;

    @Value("${transcoder.ffmpeg.temp-dir:#{systemProperties['java.io.tmpdir'] + '/ott-transcode'}}")
    private String tempDir;

    @Override
    public void execute(Long mediaId, String originUrl) {
        Path workDir = Path.of(tempDir, "media-" + mediaId);

        try {
            Files.createDirectories(workDir);
            log.info("트랜스코딩 시작 - mediaId: {}, originUrl: {}", mediaId, originUrl);

            // 1. 원본 영상을 임시 작업 디렉토리로 가져옴
            Path inputFile = videoStorage.download(originUrl, workDir);

            // 2. 해상도별 HLS 트랜스코딩 (각각 media.m3u8 + segment_XXX.ts 생성)
            for (Resolution resolution : TARGET_RESOLUTION_LIST) {
                ffmpegExecutor.execute(inputFile, workDir, resolution);
            }

            // 3. 마스터 플레이리스트 생성 (3개 variant를 참조하는 master.m3u8)
            masterPlaylistGenerator.generate(workDir, TARGET_RESOLUTION_LIST);

            // 4. 결과물을 저장소에 업로드 (output-dir/media/{mediaId}/hls/)
            String uploadedPath = videoStorage.upload(workDir, "media/" + mediaId + "/hls");

            log.info("트랜스코딩 완료 - mediaId: {}, uploadedPath: {}", mediaId, uploadedPath);

        } catch (Exception e) {
            log.error("트랜스코딩 실패 - mediaId: {}", mediaId, e);
            throw new RuntimeException("트랜스코딩 실패 - mediaId: " + mediaId, e);
        } finally {
            cleanUp(workDir);
        }
    }

    /** 임시 작업 디렉토리 삭제. 하위 파일부터 역순으로 삭제. */
    private void cleanUp(Path workDir) {
        try {
            if (Files.exists(workDir)) {
                Files.walk(workDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try { Files.deleteIfExists(path); } catch (IOException ignored) {}
                        });
                log.info("임시 디렉토리 정리 완료 - {}", workDir);
            }
        } catch (IOException e) {
            log.warn("임시 디렉토리 정리 실패 - {}", workDir, e);
        }
    }
}
