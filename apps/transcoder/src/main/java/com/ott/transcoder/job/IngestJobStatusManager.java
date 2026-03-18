package com.ott.transcoder.job;

import com.ott.common.web.exception.BusinessException;
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
import com.ott.transcoder.ffmpeg.Resolution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.ott.common.web.exception.ErrorCode.INGEST_COMMAND_NOT_FOUND;
import static com.ott.common.web.exception.ErrorCode.INGEST_JOB_NOT_FOUND;

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
    public boolean startProcessing(Long ingestJobId) {
        // 비관적 락
        IngestJob ingestJob = ingestJobRepository.findByIdForUpdate(ingestJobId)
                .orElseThrow(() -> new BusinessException(INGEST_JOB_NOT_FOUND));

        if (ingestJob.getIngestStatus().equals(IngestStatus.FAILED) || ingestJob.getIngestStatus().equals(IngestStatus.SUCCESS)) {
            return false;
        }

        if (ingestJob.getIngestStatus().equals(IngestStatus.PENDING)) {
            ingestJob.updateIngestStatus(IngestStatus.PROCESSING);
            log.info("IngestJob 상태 전이 - ingestJobId: {}, PENDING → PROCESSING", ingestJobId);
        }
        return true;
    }

    /** 완료된 트랜스코드 해상도 목록 조회 (재시도 시 master.m3u8 복원용) */
    @Transactional(readOnly = true)
    public List<Resolution> getCompletedResolutions(Long ingestJobId) {
        return ingestCommandRepository
                .findByIngestJobIdAndCommandStatus(ingestJobId, CommandStatus.COMPLETED).stream()
                .filter(ic -> ic.getCommandType() == CommandType.TRANSCODE)
                .map(ic -> Resolution.fromKey(ic.getCommandKey()))
                .toList();
    }

    /**
     * CP-5: 트랜스코드 커맨드 완료
     * - IngestCommand: PENDING → COMPLETED + outputUrl
     * - 최초 성공 시: IngestJob → PARTIAL_SUCCESS, Media → COMPLETED
     * - masterPlaylistUrl은 업로드 시 미리 설정됨 (트랜스코더에서 관리하지 않음)
     */
    @Transactional
    public void completeTranscodeCommand(Long ingestJobId, Command command, String outputUrl) {
        // 1. IngestCommand: PENDING → COMPLETED + outputUrl
        completeCommandInternal(ingestJobId, command, outputUrl);

        // 2. 최초 트랜스코딩 성공 → 미디어 활성화
        IngestJob ingestJob = findIngestJob(ingestJobId);
        if (ingestJob.getIngestStatus() == IngestStatus.PROCESSING) {
            ingestJob.updateIngestStatus(IngestStatus.PARTIAL_SUCCESS);

            Media media = ingestJob.getMedia();
            media.updateMediaStatus(MediaStatus.COMPLETED);

            log.info("미디어 활성화 - ingestJobId: {}, mediaId: {}, PROCESSING → PARTIAL_SUCCESS, Media → COMPLETED",
                    ingestJobId, media.getId());
        }
    }

    /** CP-5: 비-트랜스코드 커맨드 완료 (THUMBNAIL 등) */
    @Transactional
    public void completeCommand(Long ingestJobId, Command command, String outputUrl) {
        completeCommandInternal(ingestJobId, command, outputUrl);
    }

    private void completeCommandInternal(Long ingestJobId, Command command, String outputUrl) {
        IngestCommand ingestCommand = ingestCommandRepository
                .findByIngestJobIdAndCommandKey(ingestJobId, command.getCommandKey())
                .orElseThrow(() -> new BusinessException(INGEST_COMMAND_NOT_FOUND));
        ingestCommand.updateCommandStatus(CommandStatus.COMPLETED);
        ingestCommand.updateOutputUrl(outputUrl);

        log.info("IngestCommand 완료 - ingestJobId: {}, key: {}, outputUrl: {}",
                ingestJobId, command.getCommandKey(), outputUrl);
    }

    /** CP-6: 모든 커맨드 완료 확인 */
    @Transactional
    public void checkAllCompleted(Long ingestJobId) {
        boolean hasIncomplete = ingestCommandRepository
                .existsByIngestJobIdAndCommandStatusNot(ingestJobId, CommandStatus.COMPLETED);

        if (!hasIncomplete) {
            IngestJob ingestJob = findIngestJob(ingestJobId);
            ingestJob.updateIngestStatus(IngestStatus.SUCCESS);

            log.info("모든 커맨드 완료 - ingestJobId: {}, SUCCESS", ingestJobId);
        }
    }

    /** CP-7: 재시도 소진 → 실패 처리 */
    @Transactional
    public void fail(Long ingestJobId) {
        Optional<IngestJob> findIngestJob = ingestJobRepository.findById(ingestJobId);
        if (findIngestJob.isEmpty()) {
            log.warn("작업 실패 처리 중 해당 Job을 찾을 수 없음 - ingestJobId: {} (무시하고 종료)", ingestJobId);
            return;
        }
        IngestJob ingestJob = findIngestJob.get();

        if (ingestJob.getIngestStatus() == IngestStatus.PARTIAL_SUCCESS || ingestJob.getIngestStatus() == IngestStatus.SUCCESS) {
            log.warn("후속 커맨드 실패이지만 이미 노출 가능한 상태를 유지합니다 - ingestJobId: {}, status: {}", ingestJobId, ingestJob.getIngestStatus());
            return;
        }

        ingestJob.updateIngestStatus(IngestStatus.FAILED);

        Media media = ingestJob.getMedia();
        media.updateMediaStatus(MediaStatus.FAILED);

        log.info("작업 실패 - ingestJobId: {}, mediaId: {}, → FAILED", ingestJobId, media.getId());
    }

    private IngestJob findIngestJob(Long ingestJobId) {
        return ingestJobRepository.findById(ingestJobId)
                .orElseThrow(() -> new BusinessException(INGEST_JOB_NOT_FOUND));
    }
}
