package com.ott.api_user.media_matrics.controller;

import com.ott.api_user.media_matrics.dto.request.RadarPreferenceRequest;
import com.ott.api_user.media_matrics.dto.response.RadarMediaResponse;
import com.ott.api_user.media_matrics.dto.response.RadarPreferenceResponse;
import com.ott.api_user.media_matrics.service.RadarPreferenceService;
import com.ott.common.web.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/radar")
@RequiredArgsConstructor
public class RadarPreferenceController implements RadarPreferenceApi {

    private final RadarPreferenceService radarPreferenceService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse<RadarPreferenceResponse>> getPreference(
            @AuthenticationPrincipal Long memberId
    ) {
        return ResponseEntity.ok(
                SuccessResponse.of(radarPreferenceService.getPreference(memberId))
        );
    }

    @Override
    @PutMapping
    public ResponseEntity<Void> updatePreference(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody RadarPreferenceRequest request
    ) {
        radarPreferenceService.updatePreference(memberId, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/recommend")
    public ResponseEntity<SuccessResponse<List<RadarMediaResponse>>> getRecommendationList(
            @AuthenticationPrincipal Long memberId
    ) {
        return ResponseEntity.ok(
                SuccessResponse.of(radarPreferenceService.getRecommendations(memberId))
        );
    }
}
