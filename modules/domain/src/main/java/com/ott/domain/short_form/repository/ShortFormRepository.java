package com.ott.domain.short_form.repository;

import com.ott.domain.short_form.domain.ShortForm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortFormRepository extends JpaRepository<ShortForm, Long>, ShortFormRepositoryCustom  {
}
