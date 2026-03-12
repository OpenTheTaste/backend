CREATE TABLE IF NOT EXISTS mood_category
(
    id            BIGINT AUTO_INCREMENT    NOT NULL,
    name          VARCHAR(50)              NOT NULL,
    created_date  DATETIME                 NOT NULL,
    modified_date DATETIME                 NOT NULL,
    status        ENUM ('DELETE','ACTIVE') NOT NULL,

    CONSTRAINT pk_mood_category PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS mood_tag
(
    id               BIGINT AUTO_INCREMENT    NOT NULL,
    mood_category_id BIGINT                   NOT NULL,
    name             VARCHAR(50)              NOT NULL,
    created_date     DATETIME                 NOT NULL,
    modified_date    DATETIME                 NOT NULL,
    status           ENUM ('DELETE','ACTIVE') NOT NULL,

    CONSTRAINT pk_mood_tag PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS media_mood_tag
(
    id            BIGINT AUTO_INCREMENT    NOT NULL,
    media_id      BIGINT                   NOT NULL,
    mood_tag_id   BIGINT                   NOT NULL,
    created_date  DATETIME                 NOT NULL,
    modified_date DATETIME                 NOT NULL,
    status        ENUM ('DELETE','ACTIVE') NOT NULL,

    CONSTRAINT pk_media_mood_tag PRIMARY KEY (id)
) engine = InnoDB;

ALTER TABLE mood_tag
    ADD CONSTRAINT fk_mood_tag_to_mood_category
        FOREIGN KEY (mood_category_id)
            REFERENCES mood_category (id);

ALTER TABLE media_mood_tag
    ADD CONSTRAINT fk_media_mood_tag_to_media
        FOREIGN KEY (media_id)
            REFERENCES media (id);

ALTER TABLE media_mood_tag
    ADD CONSTRAINT fk_media_mood_tag_to_mood_tag
        FOREIGN KEY (mood_tag_id)
            REFERENCES mood_tag (id);


CREATE UNIQUE INDEX uq_media_mood_tag_media_id_mood_tag_id
      ON media_mood_tag (media_id, mood_tag_id);