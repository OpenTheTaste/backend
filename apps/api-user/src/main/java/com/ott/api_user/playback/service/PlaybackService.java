package com.ott.api_user.playback.service;

import java.util.Optional;

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
    private final MemberRepository memberRepository;

    public void updatePlayback(Long memberId, Long mediaId, Integer positionSec){
        Contents contents = contentsRepository.findByMediaId(mediaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));
        
    
        Optional<Playback> playbackOpt = playbackRepository.findByMemberIdAndMediaId(memberId, mediaId);

        if(playbackOpt.isPresent()){
            playbackOpt.get().updatePosition(positionSec);
        }else{
            Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));
            
            playbackRepository.save(
                Playback.builder()
                    .member(member)
                    .contents(contents)
                    .positionSec(positionSec)
                    .build()
            );
        }
    }
}
