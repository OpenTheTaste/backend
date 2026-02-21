package com.ott.api_user.series.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ott.api_user.series.dto.SeriesDetailResponse;
import com.ott.api_user.series.service.SeriesService;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;

import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/series")
public class SeriesController implements SeriesApi {
    private final SeriesService seriesService;

    @Override
    public ResponseEntity<SuccessResponse<SeriesDetailResponse>> getSeriesDetail(
            @PathVariable(value = "seriesId") Long seriesId, Long memberId) {

        Long currentMemberId = 1L;
        SeriesDetailResponse response = seriesService.getSeriesDetail(seriesId, currentMemberId);

        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    @Override
    public ResponseEntity<SuccessResponse<PageResponse>> getSeriesContents(
            @PathVariable(value = "seriesId") Long seriesId,
            @RequestParam(value = "page") Integer pageParam,
            @RequestParam(value = "size") Integer sizeParam,
            Long memberId) {

        Long currentMemberId = 1L;
        PageResponse response = seriesService.getSeriesContents(seriesId, pageParam, sizeParam, currentMemberId);

        return ResponseEntity.ok(SuccessResponse.of(response));
    }
}