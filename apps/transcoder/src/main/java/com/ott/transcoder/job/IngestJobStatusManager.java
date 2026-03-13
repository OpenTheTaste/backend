package com.ott.transcoder.job;

import com.ott.domain.ingest_command.domain.CommandStatus;
import com.ott.domain.ingest_command.domain.CommandType;
import com.ott.domain.ingest_command.domain.IngestCommand;
import com.ott.domain.ingest_command.repository.IngestCommandRepository;
import com.ott.domain.ingest_job.domain.IngestJob;
import com.ott.domain.ingest_job.domain.IngestStatus;
import com.ott.domain.ingest_job.repository.IngestJobRepository;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.domain.MediaStatus;
import com.ott.transcoder.command.Command;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 트랜스코딩 체크포인트별 상태 전이 담당
 * 문서 내 CP-3 ~ CP-7에 해당하는 상태 변경을 한 곳에서 관리
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class IngestJobStatusManager {

    private final IngestJobRepository ingestJobRepository;
    private final IngestCommandRepository ingestCommandRepository;

    /** CP-3: 메시지 컨슘 → 작업 시작 */
    @Transactional
    public void startProcessing(Long ingestJobId) {
        IngestJob ingestJob = findIngestJob(ingestJobId);
        ingestJob.updateIngestStatus(IngestStatus.PROCESSING);

        log.info("IngestJob 상태 전이 - ingestJobId: {}, PENDING → PROCESSING", ingestJobId);
    }

    /** CP-4: 커맨드 추출 → Insert */
    @Transactional
    public void createCommands(IngestJob ingestJob, List<Command> commandList) {
        List<IngestCommand> ingestCommandList = commandList.stream()
                .map(command -> IngestCommand.builder()
                        .ingestJob(ingestJob)
                        .commandType(command.getType())
                        .commandKey(command.getCommandKey())
                        .commandStatus(CommandStatus.PENDING)
                        .build())
                .toList();

        // TODO: Bulk INSERT로 변경
        ingestCommandRepository.saveAll(ingestCommandList);

        log.info("IngestCommand 생성 - ingestJobId: {}, count: {}", ingestJob.getId(), ingestCommandList.size());
    }

    /** CP-5: 개별 커맨드 성공 */
    @Transactional
    public void completeCommand(Long ingestJobId, Command command) {
        // 1. IngestCommand: PENDING → COMPLETED
        // TODO: IngestJobId & CommandKey Unique Key 도입 필요
        IngestCommand ingestCommand = ingestCommandRepository
                .findByIngestJobIdAndCommandKey(ingestJobId, command.getCommandKey());
        ingestCommand.updateCommandStatus(CommandStatus.COMPLETED);

        log.info("IngestCommand 완료 - ingestJobId: {}, type: {}, key: {}",
                ingestJobId, command.getType(), command.getCommandKey());

        // 2. 최초 트랜스코딩 성공 → IngestJob: PROCESSING → PARTIAL_SUCCESS + Media: INIT → COMPLETED
        if (command.getType() == CommandType.TRANSCODE) {
            IngestJob ingestJob = findIngestJob(ingestJobId);
            if (ingestJob.getIngestStatus() == IngestStatus.PROCESSING) {
                ingestJob.updateIngestStatus(IngestStatus.PARTIAL_SUCCESS);

                Media media = ingestJob.getMedia();
                media.updateMediaStatus(MediaStatus.COMPLETED);

                log.info("최초 트랜스코딩 성공 - ingestJobId: {}, mediaId: {}, PROCESSING → PARTIAL_SUCCESS, Media → COMPLETED",
                        ingestJobId, media.getId());
            }
        }
    }

    /** CP-6: 모든 커맨드 완료 확인 */
    @Transactional
    public void checkAllCompleted(Long ingestJobId) {
        boolean hasIncomplete = ingestCommandRepository
                .existsByIngestJobIdAndCommandStatusNot(ingestJobId, CommandStatus.COMPLETED);

        if (!hasIncomplete) {
            IngestJob ingestJob = findIngestJob(ingestJobId);
            ingestJob.updateIngestStatus(IngestStatus.SUCCESS);

            log.info("모든 커맨드 완료 - ingestJobId: {}, PARTIAL_SUCCESS → SUCCESS", ingestJobId);
        }
    }

    /** CP-7: 재시도 소진 → 실패 처리 */
    @Transactional
    public void fail(Long ingestJobId) {
        IngestJob ingestJob = findIngestJob(ingestJobId);
        ingestJob.updateIngestStatus(IngestStatus.FAILED);

        Media media = ingestJob.getMedia();
        media.updateMediaStatus(MediaStatus.FAILED);

        log.info("작업 실패 - ingestJobId: {}, mediaId: {}, → FAILED", ingestJobId, media.getId());
    }

    private IngestJob findIngestJob(Long ingestJobId) {
        return ingestJobRepository.findById(ingestJobId)
                .orElseThrow(() -> new IllegalStateException(
                        "IngestJob을 찾을 수 없습니다 - ingestJobId: " + ingestJobId));
    }
}
