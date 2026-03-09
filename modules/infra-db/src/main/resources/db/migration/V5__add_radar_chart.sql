-- 레이더 차트 추천용 미디어 메트릭스 테이블
-- 배치 스케줄러가 주기적으로 5개 축의 percentile rank를 계산하여 UPSERT
CREATE TABLE IF NOT EXISTS media_metrics
(
    id               BIGINT AUTO_INCREMENT    NOT NULL,
    media_id         BIGINT                   NOT NULL,
    popularity       DECIMAL(5, 2)            NOT NULL DEFAULT 0,
    immersion        DECIMAL(5, 2)            NOT NULL DEFAULT 0,
    mania            DECIMAL(5, 2)            NOT NULL DEFAULT 0,
    recency          DECIMAL(5, 2)            NOT NULL DEFAULT 0,
    re_watch         DECIMAL(5, 2)            NOT NULL DEFAULT 0,

    batch_updated_at DATETIME                 NOT NULL,

    created_date     DATETIME                 NOT NULL,
    modified_date    DATETIME                 NOT NULL,
    status           ENUM ('DELETE','ACTIVE') NOT NULL,

    CONSTRAINT pk_media_metrics PRIMARY KEY (id)
) ENGINE = InnoDB;

-- 사용자별 레이더 차트 슬라이더 설정값 (회원가입 시 생성, member와 1:1)
CREATE TABLE IF NOT EXISTS member_radar_preference
(
    id            BIGINT AUTO_INCREMENT       NOT NULL,
    member_id     BIGINT                      NOT NULL,
    popularity    INT                         NOT NULL DEFAULT 0,
    immersion     INT                         NOT NULL DEFAULT 0,
    mania         INT                         NOT NULL DEFAULT 0,
    recency       INT                         NOT NULL DEFAULT 0,
    re_watch      INT                         NOT NULL DEFAULT 0,

    created_date  DATETIME                    NOT NULL,
    modified_date DATETIME                    NOT NULL,
    status        ENUM ('DELETE','ACTIVE')    NOT NULL,

    CONSTRAINT pk_member_radar_preference PRIMARY KEY (id)
) ENGINE = InnoDB;


-- Media Metrics
ALTER TABLE media_metrics
    ADD CONSTRAINT uk_media_metrics_media
        UNIQUE (media_id);

ALTER TABLE media_metrics
    ADD CONSTRAINT fk_media_metrics_media
        FOREIGN KEY (media_id)
            REFERENCES media (id);

-- 재시청 횟수 추적 (첫 시청 = 0, 재시청마다 +1)
ALTER TABLE watch_history
    ADD COLUMN re_watch_count INT NOT NULL DEFAULT 0;

-- Member Radar Preference
ALTER TABLE member_radar_preference
    ADD CONSTRAINT uk_member_radar_preference_member
        UNIQUE (member_id);

ALTER TABLE member_radar_preference
    ADD CONSTRAINT fk_member_radar_preference_member
        FOREIGN KEY (member_id)
            REFERENCES member (id);
