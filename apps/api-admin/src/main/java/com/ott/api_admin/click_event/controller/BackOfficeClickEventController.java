package com.ott.api_admin.click_event.controller;

import com.ott.api_admin.click_event.dto.response.ShortFormConversionResponse;
import com.ott.api_admin.click_event.service.BackOfficeClickEventService;
import com.ott.common.web.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/back-office/admin")
public class BackOfficeClickEventController implements BackOfficeClickEventApi {

    private final BackOfficeClickEventService backOfficeClickEventService;

    // 이번 달 숏폼 -> 콘텐츠 전환율 및 전월 대비 증감율 조회
    @Override
    @GetMapping("/short-form-conversion")
    public ResponseEntity<SuccessResponse<ShortFormConversionResponse>> getShortFormConversionStats() {
        return ResponseEntity.ok(
                SuccessResponse.of(backOfficeClickEventService.getShortFormConversionStats())
        );
    }
}
