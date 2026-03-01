package com.ott.api_user.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentSource {
    TRENDING("TRENDING"), // 인기 차트 / 실시간 트렌딩 리스트에서 진입 시
    BOOKMARK("BOOKMARK"), // 북마크/시청 중 목록에서 진입 시
    HISTORY("HISTORY"), // 최근 시청 중인 콘텐츠에서 진입 시
    TAG("TAG"), // 특정 태그(예: #스릴러) 클릭 시
    RECOMMEND("RECOMMEND"), // "OO님이 좋아할 만한 리스트"에서 진입 시
    SEARCH("SEARCH"), // 검색 결과에서 진입 시
    SERIES("SERISE"); // 시리즈 상세 페이지에서 진입 시 

    private final String value;
}
