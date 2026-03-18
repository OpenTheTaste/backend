CREATE TABLE IF NOT EXISTS transcode_outbox
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY       NOT NULL,

    -- 메시지 페이로드
    media_id      BIGINT                                  NOT NULL,
    ingest_job_id BIGINT                                  NOT NULL,
    origin_url    VARCHAR(500)                            NOT NULL,
    file_size     BIGINT                                  NULL,
    media_type    ENUM ('CONTENTS', 'SHORT_FORM')         NOT NULL,

    -- Outbox 상태
    outbox_status ENUM ('PENDING', 'PUBLISHED', 'FAILED') NOT NULL,
    retry_count   INT                                     NOT NULL,
    max_retries   INT                                     NOT NULL,

    -- 추적 정보
    error_message VARCHAR(500)                            NULL,

    created_date  DATETIME                                NOT NULL,
    modified_date DATETIME                                NOT NULL,
    status        ENUM ('DELETE','ACTIVE')                NOT NULL,

    CONSTRAINT pk_transcode_outbox PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS shedlock
(
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at  TIMESTAMP(3) NOT NULL,
    locked_by  VARCHAR(255) NOT NULL,
    CONSTRAINT pk_shedlock PRIMARY KEY (name)
);


ALTER TABLE transcode_outbox
    ADD CONSTRAINT fk_transcode_outbox_to_ingest_job
        FOREIGN KEY (ingest_job_id)
            REFERENCES ingest_job (id);

CREATE INDEX idx_outbox_status_created_date
    ON transcode_outbox (outbox_status, created_date);
