package com.ott.domain.media_tag.repository;

import com.ott.domain.media_tag.domain.MediaTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaTagRepository extends JpaRepository<MediaTag, Long>, MediaTagRepositoryCustom {
    void deleteAllByMedia_Id(Long mediaId);
}
