package com.ott.domain.media.repository;

import com.ott.domain.media.domain.Media;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<Media, Long>, MediaRepositoryCustom {
}
