package com.ott.api_admin.shortform.service;

import com.ott.api_admin.content.vo.IngestJobResult;
import com.ott.api_admin.shortform.dto.request.ShortFormUpdateRequest;
import com.ott.api_admin.shortform.dto.request.ShortFormUploadRequest;
import com.ott.api_admin.shortform.dto.response.OriginMediaTitleListResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormDetailResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormListResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormUpdateResponse;
import com.ott.api_admin.shortform.dto.response.ShortFormUploadResponse;
import com.ott.api_admin.upload.dto.response.MultipartUploadPartUrlResponse;
import com.ott.api_admin.upload.support.UploadHelper;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.common.PublicStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ShortForm 오케스트레이션 서비스
 * 트랜잭션을 직접 갖지 않으며, Reader/Writer에 위임
 * S3 같은 외부 호출은 이 계층에서 직접 호출하여 트랜잭션 밖에서 실행
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class BackOfficeShortFormService {

    private final BackOfficeShortFormReader reader;
    private final BackOfficeShortFormWriter writer;
    private final UploadHelper uploadHelper;

    // ── 읽기 위임 ──

    public PageResponse<ShortFormListResponse> getShortFormList(
            Integer page, Integer size, String searchWord, PublicStatus publicStatus,
            Authentication authentication) {
        return reader.getShortFormList(page, size, searchWord, publicStatus, authentication);
    }

    public PageResponse<OriginMediaTitleListResponse> getOriginMediaTitle(Integer page, Integer size, String searchWord) {
        return reader.getOriginMediaTitle(page, size, searchWord);
    }

    public ShortFormDetailResponse getShortFormDetail(Long mediaId, Authentication authentication) {
        return reader.getShortFormDetail(mediaId, authentication);
    }

    public PageResponse<MultipartUploadPartUrlResponse> getShortFormOriginUploadPartUrls(
            Long shortFormId, String objectKey, String uploadId,
            Integer page, Integer size, Authentication authentication) {
        return reader.getShortFormOriginUploadPartUrls(shortFormId, objectKey, uploadId, page, size, authentication);
    }

    // ── 쓰기 위임 ──

    public ShortFormUploadResponse createShortFormUpload(ShortFormUploadRequest request, Long memberId) {
        return writer.createShortFormUpload(request, memberId);
    }

    public ShortFormUpdateResponse updateShortFormUpload(Long shortformId, ShortFormUpdateRequest request, Authentication authentication) {
        return writer.updateShortFormUpload(shortformId, request, authentication);
    }

    // ── complete ──

    public void completeShortFormOriginUpload(
            Long shortFormId, String objectKey, String uploadId,
            List<UploadHelper.MultipartPartETag> parts, Authentication authentication) {

        // Phase 1: 검증 + 권한 체크 + 정보 조회 (readOnly 트랜잭션)
        int totalPartCount = reader.getShortFormUploadInfo(shortFormId, objectKey, authentication);

        // Phase 2: S3 멀티파트 완료 (트랜잭션 밖 — 외부 호출)
        uploadHelper.completeMultipartUpload(objectKey, uploadId, totalPartCount, parts);

        // Phase 3: IngestJob 생성 (쓰기 트랜잭션)
        IngestJobResult result = writer.createIngestJob(shortFormId, objectKey);

        // Phase 4: 메시지 발행 (트랜잭션 밖)
        // TODO: RabbitMQ 의존성 추가 후 구현
        // transcodePublisher.publish(new TranscodeMessage(
        //     result.mediaId(), result.ingestJobId(),
        //     result.originObjectKey(), result.fileSize(), result.mediaType()));

        log.info("업로드 완료 + 트랜스코딩 요청 - shortFormId: {}, mediaId: {}, ingestJobId: {}",
                shortFormId, result.mediaId(), result.ingestJobId());
    }
}
