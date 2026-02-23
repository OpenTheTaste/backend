package com.ott.domain.short_form.repository;

import com.ott.domain.short_form.domain.ShortForm;

import java.util.Optional;

public interface ShortFormRepositoryCustom {

    Optional<ShortForm> findWithMediaAndUploaderByMediaId(Long mediaId);
}
