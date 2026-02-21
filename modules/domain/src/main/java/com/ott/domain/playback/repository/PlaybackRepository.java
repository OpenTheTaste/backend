package com.ott.domain.playback.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ott.domain.common.Status;
import com.ott.domain.playback.domain.Playback;

public interface PlaybackRepository extends JpaRepository<Playback, Long> {
    Optional<Playback> findByMemberIdAndContentsIdAndStatus(Long memberId, Long contentsId, Status status);
}
