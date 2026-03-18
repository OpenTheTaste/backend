package com.ott.api_admin.shortform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.ott.api_admin.content.vo.IngestJobResult;
import com.ott.api_admin.publish.RabbitTranscodePublisher;
import com.ott.api_admin.shortform.service.BackOfficeShortFormReader;
import com.ott.api_admin.shortform.service.BackOfficeShortFormService;
import com.ott.api_admin.shortform.service.BackOfficeShortFormWriter;
import com.ott.api_admin.upload.support.UploadHelper;
import com.ott.domain.common.MediaType;
import com.ott.infra.mq.TranscodeMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BackOfficeShortFormServiceTest {

    @Mock
    private BackOfficeShortFormReader reader;

    @Mock
    private BackOfficeShortFormWriter writer;

    @Mock
    private UploadHelper uploadHelper;

    @Mock
    private RabbitTranscodePublisher transcodePublisher;

    @InjectMocks
    private BackOfficeShortFormService shortFormService;

    // 숏폼 업로드 complete 흐름을 목을 이용해 단계적으로 검증
    @Test
    void completeShortFormOriginUpload_proceedsThroughAllPhases() {
        Long shortFormId = 14L;
        String objectKey = "short-forms/14/origin.mp4";
        String uploadId = "upload-xyz";
        UploadHelper.MultipartPartETag part = new UploadHelper.MultipartPartETag(1, "etag-x");

        when(reader.getShortFormUploadInfo(shortFormId, objectKey, null)).thenReturn(5);
        IngestJobResult ingestJobResult = new IngestJobResult(33L, 44L, objectKey, 5000L, MediaType.SHORT_FORM);
        when(writer.createIngestJob(shortFormId, objectKey)).thenReturn(ingestJobResult);

        shortFormService.completeShortFormOriginUpload(shortFormId, objectKey, uploadId, List.of(part), null);

        InOrder order = inOrder(reader, uploadHelper, writer, transcodePublisher);
        order.verify(reader).getShortFormUploadInfo(shortFormId, objectKey, null);
        order.verify(uploadHelper).completeMultipartUpload(objectKey, uploadId, 5, List.of(part));
        order.verify(writer).createIngestJob(shortFormId, objectKey);

        ArgumentCaptor<TranscodeMessage> messageCaptor = ArgumentCaptor.forClass(TranscodeMessage.class);
        order.verify(transcodePublisher).publish(messageCaptor.capture());
        TranscodeMessage message = messageCaptor.getValue();
        assertThat(message.mediaId()).isEqualTo(ingestJobResult.mediaId());
        assertThat(message.ingestJobId()).isEqualTo(ingestJobResult.ingestJobId());
    }
}
