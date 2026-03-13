package com.ott.domain.mood_tag.repository;

import com.ott.domain.common.Status;
import com.ott.domain.mood_tag.domain.MoodTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoodTagRepository extends JpaRepository<MoodTag, Long> {

    List<MoodTag> findByNameInAndStatus(List<String> aiTags, Status status);
}
