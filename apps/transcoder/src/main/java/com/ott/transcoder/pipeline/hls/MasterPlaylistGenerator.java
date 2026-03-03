package com.ott.transcoder.pipeline.hls;

import com.ott.domain.video_profile.domain.Resolution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** HLS 마스터 플레이리스트(master.m3u8) 생성기. ABR variant를 포함한다. */
@Slf4j
@Component
public class MasterPlaylistGenerator {

    /** 해상도별 variant 메타데이터 (대역폭, 화면 크기, 상대 경로) */
    private record Variant(int bandwidth, String resolution, String playlistPath) {}

    private static final Map<Resolution, Variant> VARIANT_MAP = Map.of(
            Resolution.P360, new Variant(800_000, "640x360", "360p/media.m3u8"),
            Resolution.P720, new Variant(2_400_000, "1280x720", "720p/media.m3u8"),
            Resolution.P1080, new Variant(4_800_000, "1920x1080", "1080p/media.m3u8")
    );

    /**
     * @param outputDir      마스터 플레이리스트를 생성할 디렉토리
     * @param resolutionList 포함할 해상도 목록
     * @return 생성된 master.m3u8 경로
     */
    public Path generate(Path outputDir, List<Resolution> resolutionList) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("#EXTM3U\n");

        for (Resolution resolution : resolutionList) {
            Variant variant = VARIANT_MAP.get(resolution);
            sb.append("#EXT-X-STREAM-INF:BANDWIDTH=").append(variant.bandwidth())
                    .append(",RESOLUTION=").append(variant.resolution()).append("\n");
            sb.append(variant.playlistPath()).append("\n");
        }

        Path masterPath = outputDir.resolve("master.m3u8");
        Files.writeString(masterPath, sb.toString());

        log.info("마스터 플레이리스트 생성 - path: {}", masterPath);
        return masterPath;
    }
}
