package com.ott.domain.series.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ott.domain.common.PublicStatus;
import com.ott.domain.common.Status;
import com.ott.domain.series.domain.Series;

public interface SeriesRepository extends JpaRepository<Series, Long>, SeriesRepositoryCustom {

        // Optional<Series> findByIdAndStatusAndMedia_PublicStatus(Long id, Status
        // status, PublicStatus publicStatus);
        @Query("SELECT s FROM Series s JOIN FETCH s.media m WHERE s.id = :id AND s.status = :status AND m.publicStatus = :publicStatus")
        Optional<Series> findByIdWithMedia(@Param("id") Long id,
                        @Param("status") Status status,
                        @Param("publicStatus") PublicStatus publicStatus);

}
