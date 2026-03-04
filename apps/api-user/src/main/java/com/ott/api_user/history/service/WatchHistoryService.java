package com.ott.api_user.history.service;

import org.springframework.stereotype.Service;

import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.watch_history.repository.WatchHistoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;



@Service
@Transactional
@RequiredArgsConstructor
public class WatchHistoryService {
    private final WatchHistoryRepository watchHistoryRepository;
    private final ContentsRepository contentsRepository;

    //사용자가 영상 클릭 시 시청 이력 생성
    
}
