package com.ott.api_user.history.service;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.common.Status;
import com.ott.domain.contents.domain.Contents;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;
import com.ott.domain.watch_history.domain.WatchHistory;
import com.ott.domain.watch_history.repository.WatchHistoryRepository;


import lombok.RequiredArgsConstructor;



@Service
@Transactional
@RequiredArgsConstructor
public class WatchHistoryService {
    private final WatchHistoryRepository watchHistoryRepository;
    private final ContentsRepository contentsRepository;
    private final MemberRepository memberRepository;

    //사용자가 영상 클릭 시 시청 이력 생성
    public void upsertWatchHistory(Long memberId, Long mediaId){
        Contents contents = contentsRepository.findByMediaId(mediaId)
                .orElseThrow(()-> new BusinessException(ErrorCode.CONTENTS_NOT_FOUND));
        
        watchHistoryRepository.upsertWatchHistory(memberId, contents.getId());

    }
    
}
