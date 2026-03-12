ALTER TABLE media_mood_tag
    ADD COLUMN priority INT NOT NULL DEFAULT 0;

INSERT INTO mood_category (id, name, created_date, modified_date, status)
VALUES
    (1, '슬픔', NOW(), NOW(), 'ACTIVE'),
    (2, '공포', NOW(), NOW(), 'ACTIVE'),
    (3, '유쾌', NOW(), NOW(), 'ACTIVE'),
    (4, '힐링', NOW(), NOW(), 'ACTIVE'),
    (5, '설렘', NOW(), NOW(), 'ACTIVE'),
    (6, '지식', NOW(), NOW(), 'ACTIVE'),
    (7, '자극', NOW(), NOW(), 'ACTIVE'),
    (8, '도파민', NOW(), NOW(), 'ACTIVE')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    modified_date = NOW(),
    status = VALUES(status);

INSERT INTO mood_tag (id, mood_category_id, name, created_date, modified_date, status)
VALUES
    (1, 1, '오열', NOW(), NOW(), 'ACTIVE'),
    (2, 1, '우울', NOW(), NOW(), 'ACTIVE'),
    (3, 1, '슬픔', NOW(), NOW(), 'ACTIVE'),
    (4, 1, '감동', NOW(), NOW(), 'ACTIVE'),
    (5, 1, '에너지소모큼', NOW(), NOW(), 'ACTIVE'),
    (6, 2, '공포', NOW(), NOW(), 'ACTIVE'),
    (7, 2, '긴장', NOW(), NOW(), 'ACTIVE'),
    (8, 2, '잔인', NOW(), NOW(), 'ACTIVE'),
    (9, 2, '심장_쫄깃한', NOW(), NOW(), 'ACTIVE'),
    (10, 3, '가벼운_웃음', NOW(), NOW(), 'ACTIVE'),
    (11, 3, '유쾌한', NOW(), NOW(), 'ACTIVE'),
    (12, 3, '통쾌', NOW(), NOW(), 'ACTIVE'),
    (13, 3, '유쾌한_티키타카', NOW(), NOW(), 'ACTIVE'),
    (14, 3, '사이다_전개', NOW(), NOW(), 'ACTIVE'),
    (15, 4, '힐링', NOW(), NOW(), 'ACTIVE'),
    (16, 4, '평화로운', NOW(), NOW(), 'ACTIVE'),
    (17, 4, '잔잔한', NOW(), NOW(), 'ACTIVE'),
    (18, 4, '잔잔한_위로', NOW(), NOW(), 'ACTIVE'),
    (19, 4, '시각적_힐링', NOW(), NOW(), 'ACTIVE'),
    (20, 4, '뽀송뽀송한', NOW(), NOW(), 'ACTIVE'),
    (21, 5, '설렘', NOW(), NOW(), 'ACTIVE'),
    (22, 5, '달달함', NOW(), NOW(), 'ACTIVE'),
    (23, 5, '몽글몽글한', NOW(), NOW(), 'ACTIVE'),
    (24, 6, '딱딱함', NOW(), NOW(), 'ACTIVE'),
    (25, 7, '자극적인', NOW(), NOW(), 'ACTIVE'),
    (26, 7, '매운맛', NOW(), NOW(), 'ACTIVE'),
    (27, 7, '팝콘무비', NOW(), NOW(), 'ACTIVE'),
    (28, 8, '도파민_폭발', NOW(), NOW(), 'ACTIVE'),
    (29, 8, '도파민_디톡스', NOW(), NOW(), 'ACTIVE'),
    (30, 8, '뇌빼고_보는', NOW(), NOW(), 'ACTIVE'),
    (31, 8, '심장_안정기', NOW(), NOW(), 'ACTIVE')
ON DUPLICATE KEY UPDATE
    mood_category_id = VALUES(mood_category_id),
    name = VALUES(name),
    modified_date = NOW(),
    status = VALUES(status);
