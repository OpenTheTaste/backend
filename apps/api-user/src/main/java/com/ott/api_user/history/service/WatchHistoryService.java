package com.ott.api_user.history.service;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.contents.domain.Contents;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;
import com.ott.domain.watch_history.domain.WatchHistory;
import com.ott.domain.watch_history.repository.WatchHistoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;



@Service
@Transactional
@RequiredArgsConstructor
public class WatchHistoryService {
    private final WatchHistoryRepository watchHistoryRepository;
    private final ContentsRepository contentsRepository;
    private final MemberRepository memberRepository;

    //사용자가 영상 클릭 시 시청 이력 생성
    public void updateWatchHistory(Long memberId, Long mediaId){
        Contents contents = contentsRepository.findByMediaId(mediaId)
                .orElseThrow(()-> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));
        
        Optional<WatchHistory> watchhistoryOpt = watchHistoryRepository.findByMemberIdAndContentsId(memberId, contents.getId());

        if(watchhistoryOpt.isPresent()){
            watchhistoryOpt.get().updateLastWatchedAt();
        }else{
            Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            watchHistoryRepository.save(
                WatchHistory.builder()
                    .member(member)
                    .contents(contents)
                    .lastWatchedAt(LocalDateTime.now())
                    .build()
            );
        }
    }
    
}
