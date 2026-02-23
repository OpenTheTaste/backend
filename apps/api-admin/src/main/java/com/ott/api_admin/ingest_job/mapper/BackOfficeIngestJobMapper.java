package com.ott.api_admin.ingest_job.mapper;

import com.ott.api_admin.ingest_job.dto.response.IngestJobListResponse;
import com.ott.domain.ingest_job.domain.IngestJob;
import com.ott.domain.media.domain.Media;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BackOfficeIngestJobMapper {

    public IngestJobListResponse toIngestJobListResponse(IngestJob ingestJob, Map<Long, Integer> videoSizeByMediaId) {
        Media media = ingestJob.getMedia();

        return new IngestJobListResponse(
                ingestJob.getId(),
                media.getTitle(),
                videoSizeByMediaId.getOrDefault(media.getId(), null),
                media.getUploader().getNickname(),
                ingestJob.getIngestStatus(),
                0
        );
    }
}
