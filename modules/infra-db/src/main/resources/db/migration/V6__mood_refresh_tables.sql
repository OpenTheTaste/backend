-- 1. 기존 테이블 컬럼 추가
ALTER TABLE watch_history
    ADD COLUMN is_used_for_ml BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. 신규 테이블 생성 (규칙 적용)
CREATE TABLE IF NOT EXISTS member_mood_refresh
(
    id                    BIGINT AUTO_INCREMENT    NOT NULL,
    member_id             BIGINT                   NOT NULL,
    image_id              TINYINT                  NOT NULL,
    subtitle              TEXT                     NULL,
    recommended_media_ids JSON                     NULL,
    is_exposed            BOOLEAN                  NOT NULL DEFAULT FALSE,

    -- 공통 BaseEntity 컬럼 (DATETIME 및 ENUM 적용)
    created_date          DATETIME                 NOT NULL,
    modified_date         DATETIME                 NOT NULL,
    status                ENUM ('DELETE','ACTIVE') NOT NULL,

    -- PK 규칙 적용
    CONSTRAINT pk_member_mood_refresh PRIMARY KEY (id)
) engine = InnoDB;

-- 3. 외래키(FK) 설정 (기존 FK 모음 쪽에 함께 추가)
ALTER TABLE member_mood_refresh
    ADD CONSTRAINT fk_member_mood_refresh_to_member
        FOREIGN KEY (member_id)
            REFERENCES member (id);