package com.ott.domain.media.repository;

import com.ott.domain.media.domain.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MediaRepository extends JpaRepository<Media, Long>, MediaRepositoryCustom {

}
