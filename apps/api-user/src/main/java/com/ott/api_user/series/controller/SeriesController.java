package com.ott.api_user.series.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ott.api_user.series.dto.SeriesDetailResponse;
import com.ott.api_user.series.service.SeriesService;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/series")
public class SeriesController implements SeriesApi {
    private final SeriesService seriesService;

    @Override
    public ResponseEntity<SuccessResponse<SeriesDetailResponse>> getSeriesDetail(
            @PathVariable(value = "seriesId") Long seriesId,
            @AuthenticationPrincipal Long memberId) {

        SeriesDetailResponse response = seriesService.getSeriesDetail(seriesId, memberId);

        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    @Override
    public ResponseEntity<SuccessResponse<PageResponse>> getSeriesContents(
            @PathVariable(value = "seriesId") Long seriesId,
            @RequestParam(value = "page") Integer pageParam,
            @RequestParam(value = "size") Integer sizeParam,
            @AuthenticationPrincipal Long memberId) {

        PageResponse response = seriesService.getSeriesContents(seriesId, pageParam, sizeParam, memberId);

        return ResponseEntity.ok(SuccessResponse.of(response));
    }
}