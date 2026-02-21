package com.ott.domain.preferred_tag.repository;

import com.ott.domain.preferred_tag.domain.PreferredTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferredTagRepository extends JpaRepository<PreferredTag, Long> {
    boolean existsByMemberId(Long memberId);
}
