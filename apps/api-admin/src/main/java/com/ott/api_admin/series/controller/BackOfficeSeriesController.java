package com.ott.api_admin.series.controller;

import com.ott.api_admin.series.dto.response.SeriesDetailResponse;
import com.ott.api_admin.series.dto.response.SeriesListResponse;
import com.ott.api_admin.series.dto.response.SeriesTitleListResponse;
import com.ott.api_admin.series.service.BackOfficeSeriesService;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/back-office/admin/series")
@RequiredArgsConstructor
public class BackOfficeSeriesController implements BackOfficeSeriesApi {

    private final BackOfficeSeriesService backOfficeSeriesService;

    @Override
    @GetMapping
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
    @GetMapping("/titles")
    public ResponseEntity<SuccessResponse<PageResponse<SeriesTitleListResponse>>> getSeriesTitle(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "searchWord", required = false) String searchWord
    ) {
        return ResponseEntity.ok(
                SuccessResponse.of(backOfficeSeriesService.getSeriesTitle(page, size, searchWord))
        );
    }

    @Override
    @GetMapping("/{mediaId}")
    public ResponseEntity<SuccessResponse<SeriesDetailResponse>> getSeriesDetail(@PathVariable("mediaId") Long mediaId) {
        return ResponseEntity.ok(
                SuccessResponse.of(backOfficeSeriesService.getSeriesDetail(mediaId))
        );
    }
}
