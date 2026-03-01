package com.ott.domain.media.repository;

import com.ott.domain.common.MediaType;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.media.domain.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MediaRepositoryCustom {

    Page<Media> findMediaListByMediaTypeAndSearchWord(Pageable pageable, MediaType mediaType, String searchWord);

    Page<Media> findMediaListByMediaTypeAndSearchWordAndPublicStatus(Pageable pageable, MediaType mediaType,
            String searchWord, PublicStatus publicStatus);

    Page<Media> findMediaListByMediaTypeAndSearchWordAndPublicStatusAndUploaderId(Pageable pageable,
            MediaType mediaType, String searchWord, PublicStatus publicStatus, Long uploaderId);

    Page<Media> findOriginMediaListBySearchWord(Pageable pageable, String searchWord);
}
