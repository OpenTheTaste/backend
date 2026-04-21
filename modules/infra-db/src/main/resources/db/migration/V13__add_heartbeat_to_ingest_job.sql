-- 워커 생존 확인용 하트비트 컬럼 추가
-- CAS 선점 시 heartbeat_at 만료 여부로 워커 생존/사망 판단
ALTER TABLE ingest_job
    ADD COLUMN heartbeat_at DATETIME NULL;
