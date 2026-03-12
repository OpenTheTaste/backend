package com.ott.domain.media_mood_tag.repository;

import com.ott.domain.media_mood_tag.domain.MediaMoodTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaMoodTagRepository extends JpaRepository<MediaMoodTag, Long> {
    void deleteByMedia_Id(Long mediaId);
}
