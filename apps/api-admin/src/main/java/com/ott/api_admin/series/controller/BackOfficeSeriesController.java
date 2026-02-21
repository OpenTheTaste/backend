package com.ott.api_admin.series.controller;

import com.ott.api_admin.series.dto.response.SeriesDetailResponse;
import com.ott.api_admin.series.dto.response.SeriesListResponse;
import com.ott.api_admin.series.service.BackOfficeSeriesService;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/back-office")
@RequiredArgsConstructor
public class BackOfficeSeriesController implements BackOfficeSeriesApi {

    private final BackOfficeSeriesService backOfficeSeriesService;

    @Override
    @GetMapping("/admin/series")
    public ResponseEntity<SuccessResponse<PageResponse<SeriesListResponse>>> getSeries(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "searchWord", required = false) String searchWord
    ) {
        return ResponseEntity.ok(
                SuccessResponse.of(backOfficeSeriesService.getSeries(page, size, searchWord))
        );
    }

    @Override
    @GetMapping("/admin/series/{seriesId}")
    public ResponseEntity<SuccessResponse<SeriesDetailResponse>> getSeriesDetail(@PathVariable("seriesId") Long seriesId) {
        return ResponseEntity.ok(
                SuccessResponse.of(backOfficeSeriesService.getSeriesDetail(seriesId))
        );
    }
}
