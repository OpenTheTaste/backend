package com.ott.domain.ingest_command.domain;

import com.ott.domain.common.BaseEntity;
import com.ott.domain.ingest_job.domain.IngestJob;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Getter
@Table(
    name = "ingest_command",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_ingest_command_job_key",
            columnNames = {"ingest_job_id", "command_key"}
        )
    }
)
public class IngestCommand extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingest_job_id", nullable = false)
    private IngestJob ingestJob;

    @Enumerated(EnumType.STRING)
    @Column(name = "command_type", nullable = false)
    private CommandType commandType;

    @Column(name = "command_key", nullable = false, length = 50)
    private String commandKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "command_status", nullable = false)
    private CommandStatus commandStatus;

    @Column(name = "output_url", columnDefinition = "TEXT")
    private String outputUrl;

    public void updateCommandStatus(CommandStatus commandStatus) {
        this.commandStatus = commandStatus;
    }

    public void updateOutputUrl(String outputUrl) {
        this.outputUrl = outputUrl;
    }
}
