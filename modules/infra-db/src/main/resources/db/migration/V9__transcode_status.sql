-- ============================================================
-- V9: 트랜스코딩 상태 관리 스키마 변경
-- 1) media.media_status 컬럼 추가
-- 2) ingest_job.ingest_status ENUM 변경
-- 3) ingest_command 테이블 신규 생성
-- ============================================================

-- 1. media 테이블에 media_status 컬럼 추가
ALTER TABLE media
    ADD COLUMN media_status ENUM ('INIT','COMPLETED','FAILED') NOT NULL DEFAULT 'INIT';

-- 임시로 VARCHAR로 변경하여 어떤 문자열이든 들어갈 수 있게 만듦
ALTER
    TABLE ingest_job MODIFY COLUMN ingest_status VARCHAR(50);

SET SQL_SAFE_UPDATES = 0;

-- 안전하게 데이터 업데이트
UPDATE ingest_job
    SET ingest_status = 'PENDING';

SET SQL_SAFE_UPDATES = 1;

-- 2. ingest_job.ingest_status ENUM 변경
ALTER TABLE ingest_job
    MODIFY COLUMN ingest_status
        ENUM ('PENDING','PROCESSING','PARTIAL_SUCCESS','SUCCESS','FAILED') NOT NULL;

-- 3. ingest_command 테이블 생성
CREATE TABLE IF NOT EXISTS ingest_command
(
    id             BIGINT AUTO_INCREMENT                NOT NULL,
    ingest_job_id  BIGINT                               NOT NULL,
    command_type   ENUM ('TRANSCODE','THUMBNAIL')       NOT NULL,
    command_key    VARCHAR(50)                          NOT NULL,
    output_url     TEXT                                 NULL,
    command_status ENUM ('PENDING','COMPLETED')         NOT NULL,

    created_date   DATETIME                             NOT NULL,
    modified_date  DATETIME                             NOT NULL,
    status         ENUM ('DELETE','ACTIVE')             NOT NULL,

    CONSTRAINT pk_ingest_command PRIMARY KEY (id)
) engine = InnoDB;

ALTER TABLE ingest_command
    ADD CONSTRAINT fk_ingest_command_to_ingest_job
        FOREIGN KEY (ingest_job_id)
            REFERENCES ingest_job (id);

-- 4. video_profile 테이블 삭제 (ingest_command.output_url로 대체)
DROP TABLE IF EXISTS video_profile;
