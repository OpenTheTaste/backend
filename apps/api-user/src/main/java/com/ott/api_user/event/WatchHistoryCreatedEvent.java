package com.ott.api_user.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 클라이언트에게 204 를 바로 반환할 수 있도록
// 이벤트 리스너를 사용해 요청쓰레드에 대해 비동기 처리
@Getter
@AllArgsConstructor
public class WatchHistoryCreatedEvent {
    private Long memberId;
}
