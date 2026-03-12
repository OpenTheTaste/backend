package com.ott.domain.mood_tag.repository;

import com.ott.domain.common.Status;
import com.ott.domain.mood_category.domain.MoodCategory;
import com.ott.domain.mood_tag.domain.MoodTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MoodTagRepository extends JpaRepository<MoodTag, Long> {

}
