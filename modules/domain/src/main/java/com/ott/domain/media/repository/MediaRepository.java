package com.ott.domain.media.repository;

import com.ott.domain.media.domain.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface MediaRepository extends JpaRepository<Media, Long>, MediaRepositoryCustom {

    
}
