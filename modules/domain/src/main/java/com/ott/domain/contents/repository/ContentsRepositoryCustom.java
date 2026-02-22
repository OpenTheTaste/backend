package com.ott.domain.contents.repository;

import com.ott.domain.contents.domain.Contents;

import java.util.Optional;

public interface ContentsRepositoryCustom {

    Optional<Contents> findWithMediaAndUploaderByMediaId(Long mediaId);
}
