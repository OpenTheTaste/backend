CREATE TABLE IF NOT EXISTS member
(
    id            BIGINT AUTO_INCREMENT                        NOT NULL,
    email         VARCHAR(255)                                 NOT NULL,
    password      VARCHAR(255)                                 NULL,
    nickname      VARCHAR(255)                                 NOT NULL,
    role          ENUM ('MEMBER','EDITOR','ADMIN','SUSPENDED') NOT NULL,

    provider      ENUM ('LOCAL','KAKAO')                       NOT NULL,
    provider_id   VARCHAR(255)                                 NULL,
    refresh_token VARCHAR(255)                                 NULL,

    created_date  DATETIME                                     NOT NULL,
    modified_date DATETIME                                     NOT NULL,
    status        ENUM ('DELETE','ACTIVE')                     NOT NULL,

    CONSTRAINT pk_member PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS series
(
    id             BIGINT AUTO_INCREMENT     NOT NULL,
    uploader_id    BIGINT                    NOT NULL,
    title          VARCHAR(255)              NOT NULL,
    description    VARCHAR(255)              NOT NULL,
    actors         VARCHAR(255)              NOT NULL,
    poster_url     TEXT                      NOT NULL,
    thumbnail_url  TEXT                      NOT NULL,

    bookmark_count BIGINT                    NOT NULL DEFAULT 0,
    likes_count    BIGINT                    NOT NULL DEFAULT 0,
    public_status  ENUM ('PUBLIC','PRIVATE') NOT NULL,

    created_date   DATETIME                  NOT NULL,
    modified_date  DATETIME                  NOT NULL,
    status         ENUM ('DELETE','ACTIVE')  NOT NULL,

    CONSTRAINT pk_series PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS contents
(
    id                  BIGINT AUTO_INCREMENT     NOT NULL,
    uploader_id         BIGINT                    NOT NULL,
    series_id           BIGINT                    NULL,
    title               VARCHAR(255)              NOT NULL,
    description         VARCHAR(255)              NOT NULL,
    actors              VARCHAR(255)              NOT NULL,
    poster_url          TEXT                      NOT NULL,
    thumbnail_url       TEXT                      NOT NULL,

    duration            INT                       NULL,
    video_size          INT                       NULL,
    bookmark_count      BIGINT                    NOT NULL DEFAULT 0,
    likes_count         BIGINT                    NOT NULL DEFAULT 0,
    public_status       ENUM ('PUBLIC','PRIVATE') NOT NULL,
    origin_url          TEXT                      NOT NULL,
    master_playlist_url TEXT                      NULL,

    created_date        DATETIME                  NOT NULL,
    modified_date       DATETIME                  NOT NULL,
    status              ENUM ('DELETE','ACTIVE')  NOT NULL,

    CONSTRAINT pk_contents PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS short_form
(
    id                  BIGINT AUTO_INCREMENT     NOT NULL,
    uploader_id         BIGINT                    NOT NULL,
    series_id           BIGINT                    NULL,
    contents_id         BIGINT                    NULL,
    title               VARCHAR(255)              NOT NULL,
    description         VARCHAR(255)              NOT NULL,
    poster_url          TEXT                      NOT NULL,

    duration            INT                       NULL,
    video_size          INT                       NULL,
    bookmark_count      BIGINT                    NOT NULL DEFAULT 0,
    public_status       ENUM ('PUBLIC','PRIVATE') NOT NULL,
    origin_url          TEXT                      NOT NULL,
    master_playlist_url TEXT                      NULL,

    created_date        DATETIME                  NOT NULL,
    modified_date       DATETIME                  NOT NULL,
    status              ENUM ('DELETE','ACTIVE')  NOT NULL,

    CONSTRAINT pk_short_form PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS category
(
    id            BIGINT AUTO_INCREMENT    NOT NULL,
    name          VARCHAR(255)             NOT NULL,

    created_date  DATETIME                 NOT NULL,
    modified_date DATETIME                 NOT NULL,
    status        ENUM ('DELETE','ACTIVE') NOT NULL,

    CONSTRAINT pk_category PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS tag
(
    id            BIGINT AUTO_INCREMENT    NOT NULL,
    category_id   BIGINT                   NOT NULL,
    name          VARCHAR(255)             NOT NULL,

    created_date  DATETIME                 NOT NULL,
    modified_date DATETIME                 NOT NULL,
    status        ENUM ('DELETE','ACTIVE') NOT NULL,

    CONSTRAINT pk_tag PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS preferred_tag
(
    id            BIGINT AUTO_INCREMENT    NOT NULL,
    member_id     BIGINT                   NOT NULL,
    tag_id        BIGINT                   NOT NULL,

    created_date  DATETIME                 NOT NULL,
    modified_date DATETIME                 NOT NULL,
    status        ENUM ('DELETE','ACTIVE') NOT NULL,

    CONSTRAINT pk_preferred_tag PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS series_tag
(
    id            BIGINT AUTO_INCREMENT    NOT NULL,
    tag_id        BIGINT                   NOT NULL,
    series_id     BIGINT                   NOT NULL,

    created_date  DATETIME                 NOT NULL,
    modified_date DATETIME                 NOT NULL,
    status        ENUM ('DELETE','ACTIVE') NOT NULL,

    CONSTRAINT pk_series_tag PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS contents_tag
(
    id            BIGINT AUTO_INCREMENT    NOT NULL,
    tag_id        BIGINT                   NOT NULL,
    contents_id   BIGINT                   NOT NULL,

    created_date  DATETIME                 NOT NULL,
    modified_date DATETIME                 NOT NULL,
    status        ENUM ('DELETE','ACTIVE') NOT NULL,

    CONSTRAINT pk_contents_tag PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS bookmark
(
    id            BIGINT AUTO_INCREMENT                   NOT NULL,
    member_id     BIGINT                                  NOT NULL,
    target_id     BIGINT                                  NOT NULL,
    target_type   ENUM ('SHORT_FORM','CONTENTS','SERIES') NOT NULL,

    created_date  DATETIME                                NOT NULL,
    modified_date DATETIME                                NOT NULL,
    status        ENUM ('DELETE','ACTIVE')                NOT NULL,

    CONSTRAINT pk_bookmark PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS likes
(
    id            BIGINT AUTO_INCREMENT                   NOT NULL,
    member_id     BIGINT                                  NOT NULL,
    target_id     BIGINT                                  NOT NULL,
    target_type   ENUM ('SHORT_FORM','CONTENTS','SERIES') NOT NULL,

    created_date  DATETIME                                NOT NULL,
    modified_date DATETIME                                NOT NULL,
    status        ENUM ('DELETE','ACTIVE')                NOT NULL,

    CONSTRAINT pk_likes PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS comment
(
    id            BIGINT AUTO_INCREMENT    NOT NULL,
    member_id     BIGINT                   NOT NULL,
    contents_id   BIGINT                   NOT NULL,
    content       VARCHAR(255)             NOT NULL,
    is_spoiler    BOOLEAN                  NOT NULL,

    created_date  DATETIME                 NOT NULL,
    modified_date DATETIME                 NOT NULL,
    status        ENUM ('DELETE','ACTIVE') NOT NULL,

    CONSTRAINT pk_comment PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS watch_history
(
    id              BIGINT AUTO_INCREMENT    NOT NULL,
    member_id       BIGINT                   NOT NULL,
    contents_id     BIGINT                   NOT NULL,
    last_watched_at DATETIME                 NULL,

    created_date    DATETIME                 NOT NULL,
    modified_date   DATETIME                 NOT NULL,
    status          ENUM ('DELETE','ACTIVE') NOT NULL,

    CONSTRAINT pk_watch_history PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS playback
(
    id            BIGINT AUTO_INCREMENT    NOT NULL,
    member_id     BIGINT                   NOT NULL,
    contents_id   BIGINT                   NOT NULL,
    position_sec  INT                      NOT NULL,

    created_date  DATETIME                 NOT NULL,
    modified_date DATETIME                 NOT NULL,
    status        ENUM ('DELETE','ACTIVE') NOT NULL,

    CONSTRAINT pk_playback PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS click_event
(
    id            BIGINT AUTO_INCREMENT            NOT NULL,
    member_id     BIGINT                           NOT NULL,
    short_form_id BIGINT                           NOT NULL,
    click_at      DATETIME                         NOT NULL,
    click_type    ENUM ('SHORT_CLICK','CTA_CLICK') NOT NULL,

    created_date  DATETIME                         NOT NULL,
    modified_date DATETIME                         NOT NULL,
    status        ENUM ('DELETE','ACTIVE')         NOT NULL,

    CONSTRAINT pk_click_event PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS ingest_job
(
    id            BIGINT AUTO_INCREMENT                                          NOT NULL,
    short_form_id BIGINT                                                         NULL,
    contents_id   BIGINT                                                         NULL,
    ingest_status ENUM ('ORIGIN_UPLOADED','TRANSCODING','UPLOADING','COMPLETED') NOT NULL,

    created_date  DATETIME                                                       NOT NULL,
    modified_date DATETIME                                                       NOT NULL,
    status        ENUM ('DELETE','ACTIVE')                                       NOT NULL,

    CONSTRAINT pk_ingest_job PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS video_profile
(
    id                 BIGINT AUTO_INCREMENT        NOT NULL,
    ingest_job_id      BIGINT                       NOT NULL,
    media_playlist_url TEXT                         NOT NULL,
    resolution         ENUM ('360P','720P','1080P') NOT NULL,
    profile_status     ENUM ('COMPLETED')           NOT NULL,

    created_date       DATETIME                     NOT NULL,
    modified_date      DATETIME                     NOT NULL,
    status             ENUM ('DELETE','ACTIVE')     NOT NULL,

    CONSTRAINT pk_video_profile PRIMARY KEY (id)
) engine = InnoDB;


# FK CONSTRAINTS

ALTER TABLE series
    ADD CONSTRAINT fk_series_to_member_uploader
        FOREIGN KEY (uploader_id)
            REFERENCES member (id);

ALTER TABLE contents
    ADD CONSTRAINT fk_contents_to_member_uploader
        FOREIGN KEY (uploader_id)
            REFERENCES member (id);

ALTER TABLE contents
    ADD CONSTRAINT fk_contents_to_series
        FOREIGN KEY (series_id)
            REFERENCES series (id);

ALTER TABLE short_form
    ADD CONSTRAINT fk_short_form_to_member_uploader
        FOREIGN KEY (uploader_id)
            REFERENCES member (id);

ALTER TABLE short_form
    ADD CONSTRAINT fk_short_form_to_series
        FOREIGN KEY (series_id)
            REFERENCES series (id);

ALTER TABLE short_form
    ADD CONSTRAINT fk_short_form_to_contents
        FOREIGN KEY (contents_id)
            REFERENCES contents (id);

ALTER TABLE tag
    ADD CONSTRAINT fk_tag_to_category
        FOREIGN KEY (category_id)
            REFERENCES category (id);

ALTER TABLE preferred_tag
    ADD CONSTRAINT fk_preferred_tag_to_member
        FOREIGN KEY (member_id)
            REFERENCES member (id);

ALTER TABLE preferred_tag
    ADD CONSTRAINT fk_preferred_tag_to_tag
        FOREIGN KEY (tag_id)
            REFERENCES tag (id);

ALTER TABLE series_tag
    ADD CONSTRAINT fk_series_tag_to_tag
        FOREIGN KEY (tag_id)
            REFERENCES tag (id);

ALTER TABLE series_tag
    ADD CONSTRAINT fk_series_tag_to_series
        FOREIGN KEY (series_id)
            REFERENCES series (id);

ALTER TABLE contents_tag
    ADD CONSTRAINT fk_contents_tag_to_tag
        FOREIGN KEY (tag_id)
            REFERENCES tag (id);

ALTER TABLE contents_tag
    ADD CONSTRAINT fk_contents_tag_to_contents
        FOREIGN KEY (contents_id)
            REFERENCES contents (id);

ALTER TABLE bookmark
    ADD CONSTRAINT fk_bookmark_to_member
        FOREIGN KEY (member_id)
            REFERENCES member (id);

ALTER TABLE likes
    ADD CONSTRAINT fk_likes_to_member
        FOREIGN KEY (member_id)
            REFERENCES member (id);

ALTER TABLE comment
    ADD CONSTRAINT fk_comment_to_member
        FOREIGN KEY (member_id)
            REFERENCES member (id);

ALTER TABLE comment
    ADD CONSTRAINT fk_comment_to_contents
        FOREIGN KEY (contents_id)
            REFERENCES contents (id);

ALTER TABLE watch_history
    ADD CONSTRAINT fk_watch_history_to_member
        FOREIGN KEY (member_id)
            REFERENCES member (id);

ALTER TABLE watch_history
    ADD CONSTRAINT fk_watch_history_to_contents
        FOREIGN KEY (contents_id)
            REFERENCES contents (id);

ALTER TABLE playback
    ADD CONSTRAINT fk_playback_to_member
        FOREIGN KEY (member_id)
            REFERENCES member (id);

ALTER TABLE playback
    ADD CONSTRAINT fk_playback_to_contents
        FOREIGN KEY (contents_id)
            REFERENCES contents (id);

ALTER TABLE click_event
    ADD CONSTRAINT fk_click_event_to_member
        FOREIGN KEY (member_id)
            REFERENCES member (id);

ALTER TABLE click_event
    ADD CONSTRAINT fk_click_event_to_short_form
        FOREIGN KEY (short_form_id)
            REFERENCES short_form (id);

ALTER TABLE ingest_job
    ADD CONSTRAINT fk_ingest_job_to_short_form
        FOREIGN KEY (short_form_id)
            REFERENCES short_form (id);

ALTER TABLE ingest_job
    ADD CONSTRAINT fk_ingest_job_to_contents
        FOREIGN KEY (contents_id)
            REFERENCES contents (id);

ALTER TABLE video_profile
    ADD CONSTRAINT fk_video_profile_to_ingest_job
        FOREIGN KEY (ingest_job_id)
            REFERENCES ingest_job (id);
