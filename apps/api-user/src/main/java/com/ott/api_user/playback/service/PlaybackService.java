package com.ott.api_user.playback.service;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.contents.domain.Contents;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;
import com.ott.domain.playback.domain.Playback;
import com.ott.domain.playback.repository.PlaybackRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaybackService {
    private final PlaybackRepository playbackRepository;
    private final ContentsRepository contentsRepository;

    public void updatePlayback(Long memberId, Long mediaId, Integer positionSec){
        Contents contents = contentsRepository.findByMediaId(mediaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENTS_NOT_FOUND));
        
        //기존의 JPA 안에서의 if-else 로 조회 -> 업데이트 -> 없으면 예외처리 -> 다시 조회 -> 업데이트 
        // 위 과정 대신, 네이티브 쿼리를 통해 DB 안에서의 UP-SERT 로 수정.
        playbackRepository.upsertPlayback(memberId, contents.getId(), positionSec);
        
    }
}
