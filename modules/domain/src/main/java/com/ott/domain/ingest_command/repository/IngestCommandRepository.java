package com.ott.domain.ingest_command.repository;

import com.ott.domain.ingest_command.domain.CommandStatus;
import com.ott.domain.ingest_command.domain.IngestCommand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngestCommandRepository extends JpaRepository<IngestCommand, Long> {

    List<IngestCommand> findByIngestJobId(Long ingestJobId);

    boolean existsByIngestJobIdAndCommandStatusNot(Long ingestJobId, CommandStatus commandStatus);

    IngestCommand findByIngestJobIdAndCommandKey(Long ingestJobId, String commandKey);
}
