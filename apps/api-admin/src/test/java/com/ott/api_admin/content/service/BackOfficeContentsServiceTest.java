package com.ott.api_admin.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.ott.api_admin.content.vo.IngestJobResult;
import com.ott.api_admin.publish.RabbitTranscodePublisher;
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
class BackOfficeContentsServiceTest {

    @Mock
    private BackOfficeContentsReader reader;

    @Mock
    private BackOfficeContentsWriter writer;

    @Mock
    private UploadHelper uploadHelper;

    @Mock
    private RabbitTranscodePublisher transcodePublisher;

    @InjectMocks
    private BackOfficeContentsService contentsService;

    // 업로드 완료(complete endpoint)가 순차적으로 reader→uploadHelper→writer→publisher를 호출하는지 검증
    @Test
    void completeContentsOriginUpload_callsAllStagesAndPublishesMessage() {
        Long contentsId = 23L;
        String objectKey = "contents/23/origin.mp4";
        String uploadId = "upload-abc";
        UploadHelper.MultipartPartETag part = new UploadHelper.MultipartPartETag(1, "etag-1");

        when(reader.getContentsUploadInfo(contentsId, objectKey)).thenReturn(4);
        IngestJobResult ingestJobResult = new IngestJobResult(101L, 202L, objectKey, 4000L, MediaType.CONTENTS);
        when(writer.createIngestJob(contentsId, objectKey)).thenReturn(ingestJobResult);

        contentsService.completeContentsOriginUpload(contentsId, objectKey, uploadId, List.of(part));

        InOrder sequential = inOrder(reader, uploadHelper, writer, transcodePublisher);
        sequential.verify(reader).getContentsUploadInfo(contentsId, objectKey);
        sequential.verify(uploadHelper).completeMultipartUpload(objectKey, uploadId, 4, List.of(part));
        sequential.verify(writer).createIngestJob(contentsId, objectKey);

        ArgumentCaptor<TranscodeMessage> messageCaptor = ArgumentCaptor.forClass(TranscodeMessage.class);
        sequential.verify(transcodePublisher).publish(messageCaptor.capture());
        TranscodeMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.mediaId()).isEqualTo(ingestJobResult.mediaId());
        assertThat(sentMessage.ingestJobId()).isEqualTo(ingestJobResult.ingestJobId());
        assertThat(sentMessage.originUrl()).isEqualTo(objectKey);
    }
}
