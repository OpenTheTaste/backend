-- ============================================================
-- V2: 클래스 테이블 상속 마이그레이션
-- series, contents, short_form → media 공통 부모 도입
-- ============================================================

-- 1. media, media_tag 테이블 생성
CREATE TABLE IF NOT EXISTS media
(
    id             BIGINT AUTO_INCREMENT                    NOT NULL,
    uploader_id    BIGINT                                  NOT NULL,
    title          VARCHAR(255)                            NOT NULL,
    description    VARCHAR(255)                            NOT NULL,
    poster_url     TEXT                                    NOT NULL,
    thumbnail_url  TEXT                                    NULL,

    bookmark_count BIGINT                                  NOT NULL DEFAULT 0,
    likes_count    BIGINT                                  NOT NULL DEFAULT 0,
    media_type     ENUM ('SERIES','CONTENTS','SHORT_FORM') NOT NULL,
    public_status  ENUM ('PUBLIC','PRIVATE')               NOT NULL,

    created_date   DATETIME                                NOT NULL,
    modified_date  DATETIME                                NOT NULL,
    status         ENUM ('DELETE','ACTIVE')                NOT NULL,

    CONSTRAINT pk_media PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS media_tag
(
    id            BIGINT AUTO_INCREMENT    NOT NULL,
    tag_id        BIGINT                   NOT NULL,
    media_id      BIGINT                   NOT NULL,

    created_date  DATETIME                 NOT NULL,
    modified_date DATETIME                 NOT NULL,
    status        ENUM ('DELETE','ACTIVE') NOT NULL,

    CONSTRAINT pk_media_tag PRIMARY KEY (id)
) engine = InnoDB;


-- 2. 신규 테이블 FK 설정
ALTER TABLE media
    ADD CONSTRAINT fk_media_to_member
        FOREIGN KEY (uploader_id)
            REFERENCES member (id);

ALTER TABLE media_tag
    ADD CONSTRAINT fk_media_tag_to_tag
        FOREIGN KEY (tag_id)
            REFERENCES tag (id);

ALTER TABLE media_tag
    ADD CONSTRAINT fk_media_tag_to_media
        FOREIGN KEY (media_id)
            REFERENCES media (id);


-- 3. media 상세 테이블에 media_id 컬럼 추가 + 제약조건
-- series
ALTER TABLE series
    ADD COLUMN media_id BIGINT NOT NULL AFTER id;

ALTER TABLE series
    ADD CONSTRAINT uk_series_media UNIQUE (media_id);

ALTER TABLE series
    ADD CONSTRAINT fk_series_to_media
        FOREIGN KEY (media_id)
            REFERENCES media (id);

-- contents
ALTER TABLE contents
    ADD COLUMN media_id BIGINT NOT NULL AFTER id;

ALTER TABLE contents
    ADD CONSTRAINT uk_contents_media UNIQUE (media_id);

ALTER TABLE contents
    ADD CONSTRAINT fk_contents_to_media
        FOREIGN KEY (media_id)
            REFERENCES media (id);

-- short_form
ALTER TABLE short_form
    ADD COLUMN media_id BIGINT NOT NULL AFTER id;

ALTER TABLE short_form
    ADD CONSTRAINT uk_short_form_media UNIQUE (media_id);

ALTER TABLE short_form
    ADD CONSTRAINT fk_short_form_to_media
        FOREIGN KEY (media_id)
            REFERENCES media (id);


-- 4. bookmark: target_type + target_id → media_id
ALTER TABLE bookmark
    ADD COLUMN media_id BIGINT NOT NULL;

ALTER TABLE bookmark
    ADD CONSTRAINT fk_bookmark_to_media
        FOREIGN KEY (media_id)
            REFERENCES media (id);

ALTER TABLE bookmark
    DROP COLUMN target_id;

ALTER TABLE bookmark
    DROP COLUMN target_type;


-- 5. likes: target_type + target_id → media_id
ALTER TABLE likes
    ADD COLUMN media_id BIGINT NOT NULL;

ALTER TABLE likes
    ADD CONSTRAINT fk_likes_to_media
        FOREIGN KEY (media_id)
            REFERENCES media (id);

ALTER TABLE likes
    DROP COLUMN target_id;

ALTER TABLE likes
    DROP COLUMN target_type;


-- 6. ingest_job: contents_id + short_form_id → media_id
ALTER TABLE ingest_job
    ADD COLUMN media_id BIGINT NOT NULL;

ALTER TABLE ingest_job
    ADD CONSTRAINT fk_ingest_job_to_media
        FOREIGN KEY (media_id)
            REFERENCES media (id);

ALTER TABLE ingest_job
    DROP FOREIGN KEY fk_ingest_job_to_contents;

ALTER TABLE ingest_job
    DROP FOREIGN KEY fk_ingest_job_to_short_form;

ALTER TABLE ingest_job
    DROP COLUMN contents_id;

ALTER TABLE ingest_job
    DROP COLUMN short_form_id;


-- 7. series_tag + contents_tag → media_tag 통합 (테이블 삭제)
DROP TABLE series_tag;
DROP TABLE contents_tag;


-- 8. 상세 테이블에서 media로 이동한 컬럼 제거
-- series
ALTER TABLE series
    DROP FOREIGN KEY fk_series_to_member_uploader;

ALTER TABLE series
    DROP COLUMN uploader_id,
    DROP COLUMN title,
    DROP COLUMN description,
    DROP COLUMN poster_url,
    DROP COLUMN thumbnail_url,
    DROP COLUMN bookmark_count,
    DROP COLUMN likes_count,
    DROP COLUMN public_status;

-- contents
ALTER TABLE contents
    DROP FOREIGN KEY fk_contents_to_member_uploader;

ALTER TABLE contents
    DROP COLUMN uploader_id,
    DROP COLUMN title,
    DROP COLUMN description,
    DROP COLUMN poster_url,
    DROP COLUMN thumbnail_url,
    DROP COLUMN bookmark_count,
    DROP COLUMN likes_count,
    DROP COLUMN public_status;

-- short_form
ALTER TABLE short_form
    DROP FOREIGN KEY fk_short_form_to_member_uploader;

ALTER TABLE short_form
    DROP COLUMN uploader_id,
    DROP COLUMN title,
    DROP COLUMN description,
    DROP COLUMN poster_url,
    DROP COLUMN bookmark_count,
    DROP COLUMN public_status;
