ALTER TABLE ingest_command
    ADD CONSTRAINT uk_ingest_command_job_key UNIQUE (ingest_job_id, command_key);
