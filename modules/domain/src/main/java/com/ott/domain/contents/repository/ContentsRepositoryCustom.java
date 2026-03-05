package com.ott.domain.contents.repository;

import com.ott.domain.contents.domain.Contents;

import java.util.List;
import java.util.Optional;

public interface ContentsRepositoryCustom {

    Optional<Contents> findWithMediaById(Long contentsId);

    Optional<Contents> findWithMediaAndUploaderByMediaId(Long mediaId);

    List<Contents> findAllByMediaIdIn(List<Long> mediaIdList);
}
