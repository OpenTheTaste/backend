package com.ott.domain.media.repository;

import com.ott.domain.common.MediaType;
import com.ott.domain.media.domain.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MediaRepositoryCustom {

    Page<Media> findMediaListByMediaType(Pageable pageable, MediaType mediaType, String searchWord);
}
