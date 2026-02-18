package com.ott.api_admin.series.controller;

import com.ott.api_admin.series.service.BackOfficeSeriesService;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/back-office")
@RequiredArgsConstructor
public class BackOfficeSeriesController implements BackOfficeSeriesApi {

    private final BackOfficeSeriesService backOfficeSeriesService;

    @Override
    @GetMapping("/admin/series")
    public ResponseEntity<SuccessResponse<PageResponse>> getSeries(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(
                SuccessResponse.of(backOfficeSeriesService.getSeries(page, size))
        );
    }
}
