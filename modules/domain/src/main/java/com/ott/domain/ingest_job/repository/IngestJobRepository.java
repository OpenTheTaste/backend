package com.ott.domain.ingest_job.repository;

import com.ott.domain.ingest_job.domain.IngestJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestJobRepository extends JpaRepository<IngestJob, Long>, IngestJobRepositoryCustom {
}
