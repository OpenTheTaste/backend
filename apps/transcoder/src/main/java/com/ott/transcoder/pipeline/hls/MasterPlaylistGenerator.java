package com.ott.transcoder.pipeline.hls;

import com.ott.domain.video_profile.domain.Resolution;
import com.ott.transcoder.exception.TranscodeErrorCode;
import com.ott.transcoder.exception.retryable.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** HLS 마스터 플레이리스트(master.m3u8) 생성기. ABR variant를 포함한다. */
@Slf4j
@Component
public class MasterPlaylistGenerator {

    /**
     * @param outputDir      마스터 플레이리스트를 생성할 디렉토리
     * @param resolutionList 포함할 해상도 목록
     * @return 생성된 master.m3u8 경로
     */
    public Path generate(Path outputDir, List<Resolution> resolutionList) {
        StringBuilder sb = new StringBuilder();
        sb.append("#EXTM3U\n");

        for (Resolution resolution : resolutionList) {
            sb
                    .append("#EXT-X-STREAM-INF:BANDWIDTH=")
                    .append(resolution.getVideoBitrate())
                    .append(",RESOLUTION=").append(resolution.getWidth()).append("x").append(resolution.getHeight())
                    .append("\n");
            sb
                    .append(resolution.getKey().toLowerCase())
                    .append("/media.m3u8")
                    .append("\n");
        }

        Path masterPath = outputDir.resolve("master.m3u8");
        try {
            Files.writeString(masterPath, sb.toString());
        } catch (IOException e) {
            throw new StorageException(TranscodeErrorCode.STORAGE_FAILED,
                    "마스터 플레이리스트 생성 실패 - " + masterPath, e);
        }

        log.info("마스터 플레이리스트 생성 - path: {}", masterPath);
        return masterPath;
    }
}
