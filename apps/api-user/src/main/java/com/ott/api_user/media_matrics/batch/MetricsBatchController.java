package com.ott.api_user.media_matrics.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MetricsBatchController {

    private final MetricsCalculator metricsCalculator;

    @PostMapping("/radar/batch")
    public ResponseEntity<Void> triggerBatch() {
        metricsCalculator.calculate();
        return ResponseEntity.ok().build();
    }
}
