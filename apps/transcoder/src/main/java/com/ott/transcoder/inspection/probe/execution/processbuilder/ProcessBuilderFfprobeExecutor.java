package com.ott.transcoder.inspection.probe.execution.processbuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ott.transcoder.exception.TranscodeErrorCode;
import com.ott.transcoder.exception.retryable.ProbeException;
import com.ott.transcoder.inspection.probe.execution.FfprobeExecutor;
import com.ott.transcoder.inspection.probe.ProbeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ffprobe를 JSON 출력 모드로 실행하여 미디어 메타데이터를 추출
 * format(컨테이너 정보)과 streams(스트림 정보)를 함께 요청하고,
 * 첫 번째 비디오/오디오 스트림에서 필요한 필드를 파싱
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "transcoder.ffprobe.engine", havingValue = "processbuilder")
public class ProcessBuilderFfprobeExecutor implements FfprobeExecutor {

    private final ObjectMapper objectMapper;

    @Value("${transcoder.ffprobe.path:ffprobe}")
    private String ffprobePath;

    @Override
    public ProbeResult probe(Path inputFile) {
        List<String> command = List.of(
                ffprobePath,
                "-v", "quiet",
                "-print_format", "json",
                "-show_format",
                "-show_streams",
                inputFile.toString()
        );

        log.info("ffprobe 실행 - input: {}", inputFile);

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = new String(process.getInputStream().readAllBytes());

            boolean finished = process.waitFor(2, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                throw new ProbeException(TranscodeErrorCode.PROBE_TIMEOUT,
                        "ffprobe 타임아웃 - input: " + inputFile);
            }
            if (process.exitValue() != 0) {
                throw new ProbeException(TranscodeErrorCode.PROBE_FAILED,
                        "ffprobe 실패 - exitCode: " + process.exitValue() + ", output: " + output);
            }

            return parseJson(output);

        } catch (IOException | InterruptedException e) {
            throw new ProbeException(TranscodeErrorCode.PROBE_FAILED,
                    "ffprobe 실행 실패 - input: " + inputFile, e);
        }
    }

    private ProbeResult parseJson(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode streamList = root.get("streams");

        JsonNode videoStream = null;
        JsonNode audioStream = null;

        for (JsonNode stream : streamList) {
            String codecType = stream.get("codec_type").asText();
            if ("video".equals(codecType) && videoStream == null) {
                videoStream = stream;
            } else if ("audio".equals(codecType) && audioStream == null) {
                audioStream = stream;
            }
        }

        if (videoStream == null) {
            throw new ProbeException(TranscodeErrorCode.NO_VIDEO_STREAM,
                    "비디오 스트림을 찾을 수 없음");
        }

        JsonNode format = root.get("format"); // null 가능성 존재

        double duration = format.has("duration")
                ? format.get("duration").asDouble()
                : 0.0;

        double fps = parseFps(videoStream.path("r_frame_rate").asText("0/1"));

        long videoBitrate = videoStream.has("bit_rate")
                ? videoStream.get("bit_rate").asLong()
                : format.path("bit_rate").asLong(0);

        long audioBitrate = (audioStream != null && audioStream.has("bit_rate"))
                ? audioStream.get("bit_rate").asLong()
                : 0L;

        String audioCodec = (audioStream != null)
                ? audioStream.get("codec_name").asText()
                : "none";

        int audioChannels = (audioStream != null)
                ? audioStream.path("channels").asInt(0)
                : 0;

        String pixelFormat = videoStream.path("pix_fmt").asText("unknown");

        int rotation = parseRotation(videoStream);

        return new ProbeResult(
                videoStream.get("width").asInt(),
                videoStream.get("height").asInt(),
                duration,
                videoStream.get("codec_name").asText(),
                audioCodec,
                fps,
                videoBitrate,
                audioBitrate,
                audioChannels,
                pixelFormat,
                rotation
        );
    }

    /** side_data_list[].rotation → tags.rotate 순으로 확인 */
    private int parseRotation(JsonNode videoStream) {
        // 1. side_data_list에서 rotation 확인
        JsonNode sideDataList = videoStream.path("side_data_list");
        if (sideDataList.isArray()) {
            for (JsonNode sideData : sideDataList) {
                if (sideData.has("rotation")) {
                    return Math.abs(sideData.get("rotation").asInt());
                }
            }
        }

        // 2. tags.rotate 확인 (구버전 호환)
        JsonNode tags = videoStream.path("tags");
        if (tags.has("rotate")) {
            return Math.abs(tags.get("rotate").asInt());
        }

        return 0;
    }

    /** "30/1", "30000/1001" 등 분수 형태 파싱 */
    private double parseFps(String rFrameRate) {
        String[] parts = rFrameRate.split("/");
        if (parts.length == 2) {
            double numerator = Double.parseDouble(parts[0]);
            double denominator = Double.parseDouble(parts[1]);
            return denominator > 0 ? numerator / denominator : 0.0;
        }
        return Double.parseDouble(rFrameRate);
    }
}
