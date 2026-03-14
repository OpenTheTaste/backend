package com.ott.transcoder.command;

import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.ingest_command.domain.CommandStatus;
import com.ott.domain.ingest_command.domain.IngestCommand;
import com.ott.domain.ingest_command.repository.IngestCommandRepository;
import com.ott.domain.ingest_job.domain.IngestJob;
import com.ott.domain.ingest_job.repository.IngestJobRepository;
import com.ott.transcoder.ffmpeg.Resolution;
import com.ott.transcoder.inspection.probe.ProbeResult;
import com.ott.transcoder.queue.TranscodeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class CommandExtractor {

    private final IngestJobRepository ingestJobRepository;
    private final IngestCommandRepository ingestCommandRepository;

    @Transactional
    public List<Command> extractCommand(TranscodeMessage message, ProbeResult probeResult) {
        Long ingestJobId = message.ingestJobId();
        IngestJob ingestJob = ingestJobRepository.findById(ingestJobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INGEST_JOB_NOT_FOUND));

        // 1. 후보 커맨드 추출
        List<Command> candidateList = buildCandidateList(message, probeResult);

        // 2. 완료된 commandKey 조회
        Set<String> completedKeySet = ingestCommandRepository // key는 360P, 720P, thumbnail 등 고유
                .findByIngestJobIdAndCommandStatus(ingestJobId, CommandStatus.COMPLETED)
                .stream()
                .map(IngestCommand::getCommandKey)
                .collect(Collectors.toSet());

        // 3. 완료 제외 = 실행 대상
        List<Command> pendingList = candidateList.stream()
                .filter(cmd -> !completedKeySet.contains(cmd.getCommandKey()))
                .toList();

        // 4. DB에 없는 것만 저장
        Set<String> existingKeySet = ingestCommandRepository
                .findByIngestJobId(ingestJobId).stream()
                .map(IngestCommand::getCommandKey)
                .collect(Collectors.toSet());

        List<IngestCommand> newCommandList = pendingList.stream()
                .filter(cmd -> !existingKeySet.contains(cmd.getCommandKey()))
                .map(cmd -> IngestCommand.builder()
                        .ingestJob(ingestJob)
                        .commandType(cmd.getType())
                        .commandKey(cmd.getCommandKey())
                        .commandStatus(CommandStatus.PENDING)
                        .build())
                .toList();

        ingestCommandRepository.saveAll(newCommandList); // TODO: 많을 경우 Bulk INSERT

        log.info("커맨드 추출 - ingestJobId: {}, candidate: {}, completed: {}, pending: {}, newSaved: {}",
                ingestJobId, candidateList.size(), completedKeySet.size(),
                pendingList.size(), newCommandList.size());

        return pendingList;
    }

    private List<Command> buildCandidateList(TranscodeMessage message, ProbeResult probeResult) {
        List<Command> list = new ArrayList<>();
        for (Resolution resolution : Resolution.values()) {
            if (!probeResult.isUpscaleFor(resolution.getHeight())) {
                list.add(new TranscodeCommand(resolution));
            }
        }
        if (list.isEmpty()) {
            log.warn("모든 프로필 업스케일 → 360p fallback - mediaId: {}", message.mediaId());
            list.add(new TranscodeCommand(Resolution.P360));
        }

        // TODO: 추가 커맨드 추출


        return list;
    }
}
