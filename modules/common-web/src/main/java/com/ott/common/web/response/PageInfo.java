package com.ott.common.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
@Getter
public class PageInfo {

    @Schema(type = "Integer", example = "0", description = "현재 페이지. 0부터 시작. (요청한 페이지)")
    private Integer currentPage;

    @Schema(type = "Integer", example = "5", description = "전체 페이지 개수")
    private Integer totalPage;

    @Schema(type = "Integer", example = "10", description = "한 페이지의 사이즈")
    private Integer pageSize;

    @Builder
    public PageInfo(Integer currentPage, Integer totalPage, Integer pageSize) {
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        this.pageSize = pageSize;
    }
}
