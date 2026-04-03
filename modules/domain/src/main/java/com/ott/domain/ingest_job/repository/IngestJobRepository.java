package com.ott.domain.ingest_job.repository;

import com.ott.domain.ingest_job.domain.IngestJob;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IngestJobRepository extends JpaRepository<IngestJob, Long>, IngestJobRepositoryCustom {

    boolean existsByMediaId(Long mediaId);

    // SELECT ... FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT i
          FROM IngestJob i
         WHERE i.id = :ingestJobId
        """)
    Optional<IngestJob> findByIdForUpdate(@Param("ingestJobId") Long ingestJobId);
}
