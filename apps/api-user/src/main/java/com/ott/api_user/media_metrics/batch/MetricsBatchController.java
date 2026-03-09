package com.ott.api_user.media_metrics.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MetricsBatchController {

    private final MetricsCalculator metricsCalculator;

    @PostMapping("/radar/batch")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> triggerBatch() {
        metricsCalculator.calculate();
        return ResponseEntity.ok().build();
    }
}
