-- Playback 테이블에 member_id와 contents_id 조합에 대한 유니크 제약조건 추가
ALTER TABLE playback
    ADD CONSTRAINT uk_playback_member_contents UNIQUE (member_id, contents_id);



-- WatchHistory 유니크 제약조건 추가
ALTER TABLE watch_history
    ADD CONSTRAINT uk_watch_history_member_contents UNIQUE (member_id, contents_id);
