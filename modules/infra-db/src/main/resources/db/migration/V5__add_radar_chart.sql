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
