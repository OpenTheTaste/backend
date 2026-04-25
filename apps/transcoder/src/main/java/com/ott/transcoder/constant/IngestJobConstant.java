package com.ott.transcoder.constant;

public final class IngestJobConstant {
    private IngestJobConstant() {
    }

    public static final class DirectoryConstant {
        public DirectoryConstant() {
        }

        public static final String PREFIX_WORK_DIR = "media-";
        public static final String SUFFIX_WORK_DIR = "job-";
        /** 최소 해상도 (이보다 작으면 의미 없는 영상) */
        public static final int MIN_RESOLUTION = 32;

        /** 최대 해상도 (8K 초과는 비정상) */
        public static final int MAX_RESOLUTION = 8192;

        /** 최대 프레임레이트 (이보다 높으면 비정상) */
        public static final double MAX_FPS = 240.0;
    }

    public static final class ValidateConstant {
        public ValidateConstant() {
        }

        public static final String MP4 = "MP4";
        public static final String MOV = "MOV";
        public static final String WEBM = "WEBM";
        public static final String MKV = "MKV";
        public static final String AVI = "AVI";
        public static final String FLV = "FLV";
        public static final String MPEG_TS = "MPEG-TS";
    }

    public static final class VideoConstant {
        public VideoConstant() {
        }

        public static final String LIBX264 = "libx264";
        public static final String PRESET_FAST = "fast";
    }

    public static final class AudioConstant {
        public AudioConstant() {
        }

        public static final String AAC = "aac";
    }

    public static final class HeartbeatConstant {
        private HeartbeatConstant() {
        }

        /** 워커가 heartbeat_at을 갱신하는 주기 (초) */
        public static final int HEARTBEAT_INTERVAL_SEC = 10;

        /** heartbeat_at이 이 시간보다 오래되면 만료 판정 (INTERVAL * 3) */
        public static final int HEARTBEAT_TIMEOUT_SEC = 30;

        /** Delay Queue TTL (밀리초). HEARTBEAT_TIMEOUT보다 커야 함 */
        public static final int DELAY_QUEUE_TTL_MS = 40_000;
    }

    public static final class S3VideoStorageConstant {
        public S3VideoStorageConstant() {
        }

        public static final String M3U8 = ".m3u8";
        public static final String MPEGURL = "application/vnd.apple.mpegurl";
        public static final String TS = ".ts";
        public static final String MP2T = "video/mp2t";
        public static final String OCTET_STREAM = "application/octet-stream";

    }
}
