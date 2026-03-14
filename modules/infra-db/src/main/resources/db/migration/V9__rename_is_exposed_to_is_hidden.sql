-- member_mood_refresh 테이블의 is_exposed 컬럼을 엔티티에 맞게 is_hidden으로 변경
ALTER TABLE member_mood_refresh 
    CHANGE COLUMN is_exposed is_hidden BOOLEAN NOT NULL DEFAULT FALSE;