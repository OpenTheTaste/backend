package com.ott.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentSource {
    // 기본(Default) 맥락
    BOOKMARK("BOOKMARK"), // 북마크/시청 중 목록에서 진입 시
    SEARCH("SEARCH"), // 검색 결과에서 진입 시

    // 특정 진입점 맥락
    TRENDING("TRENDING"), // 인기 차트 / 실시간 트렌딩 리스트에서 진입
    TAG("TAG"), // 특정 태그(예: #스릴러) 클릭 시
    RECOMMEND("RECOMMEND"), // "OO님이 좋아할 만한 리스트"에서 진입 시

    // 시리즈 맥락
    SERIES("SERIES"); // 시리즈 상세의 에피소드 리스트에서 클릭하여 진입 시

    private final String value;
}
